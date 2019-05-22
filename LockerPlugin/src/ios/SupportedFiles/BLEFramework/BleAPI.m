//
//  BleAPI.m
//  keyPad
//
//  Created by HAOZO.MAC on 15/10/16.
//  Copyright © 2015年 HAOZO.MAC. All rights reserved.
//

#import "BleAPI.h"
#import "PublicSet.h"

#define delay 5
static BleAPI *api =nil;

@implementation BleAPI

#pragma mark - 蓝牙设备的返回处理操作
-(void)bleAPIBlocks:(bleAPIdiscoverBlock)_discover
   withconnectBlock:(bleAPIconnectBlock)_connect
withdisconnectBlock:(bleAPIdisconnectBlock)_disconnect
withnotificationBlock:(bleAPInotificationBlock)_notification
  withstopScanBlock:(bleAPStopScanBlock)_stopScan {
    
    self.discoverBlock=_discover;
    self.connectBlock=_connect;
    self.disconnectBlock=_disconnect;
    self.notificationBlock = _notification;
    self.stopScanBlock = _stopScan;
}

#pragma mark - 扫描操作 -- 开始扫瞄
-(void)scanForPer {
    
//    [self searchStopPoint];
    if (!TARGET_IPHONE_SIMULATOR) {
        NSDictionary *options = [NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO]
                                                            forKey:CBCentralManagerScanOptionAllowDuplicatesKey];
        [self.centralManager scanForPeripheralsWithServices:@[Servicesuuids]
                                                    options:options];
        [self performSelector:@selector(stopScanForPer) withObject:nil afterDelay:delay];
    }
}

//停止扫瞄
-(void)stopScanForPer{
//    NSLog(@"******扫描设备结束");
    [self.centralManager stopScan];
    self.stopScanBlock();
}

#pragma mark - 连接，断开操作  连接
-(void)didConnection:(NSArray *)devices {
    for (CBPeripheral *p in devices) {
        [self.centralManager connectPeripheral:p options:nil];
    }
}

//断开连接
-(void)didDisconnection:(NSArray *)devices{
    if (!devices.count) {
        return;
    }
    for (CBPeripheral *p in devices) {
        [self.centralManager cancelPeripheralConnection:p];
    }
}

#pragma mark - 数据写入操作
-(void)writeData:(NSData *)data forcharauuid:(NSString *)charauuid {
    if (!self.connectArrays.count) {
        return;
    }
    else{
//        NSSLog(@"----------");
        for (CBPeripheral *p in self.connectArrays) {
            
            for (CBService *service in p.services) {
                
//                NSSLog(@"services.uuid == %@",service.UUID);
                if ([service.UUID isEqual:[CBUUID UUIDWithString:@"FEE7"]]) {
                    
                    for ( CBCharacteristic *characteristic in service.characteristics ) {
                        
//                        NSLog(@"charac.uuid == %@",characteristic.UUID);
                        if ([characteristic.UUID isEqual:[CBUUID UUIDWithString:charauuid]]) {
                            
                            [p writeValue:data forCharacteristic:characteristic
                                     type:CBCharacteristicWriteWithResponse];
//                            NSLog(@"write.value :%@",data);
                        }
                    }
                }
            }
        }
    }
}

#pragma mark - ====== *********** centralManager methods and delegate ************ ==============
- (void)centralManagerDidUpdateState:(CBCentralManager *)central{
    if (central.state == CBCentralManagerStatePoweredOn) {
        [self scanForPer];
    }
    
    if (central.state == CBCentralManagerStatePoweredOff) { // 系统蓝牙关闭处理异常

        if (self.searchDeviceArrays.count) {
            for (CBPeripheral *p in self.searchDeviceArrays) {
                self.disconnectBlock(p);
            }
        }
    }
}

//发现外围设备
- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI{
    
//    NSLog(@"advertisementData :%@  name :%@ \n==============\n",advertisementData,peripheral.name);
    
    NSData *data = [advertisementData objectForKey:@"kCBAdvDataManufacturerData"];
    
//    NSArray *arr = [advertisementData objectForKey:@"kCBAdvDataServiceUUIDs"];
//    NSString *kCBAdvDataServiceUUID = arr[0];
//    NSLog(@"adv.uuid :%@  data:%@",kCBAdvDataServiceUUID,data);
//    [self getstopPointMac:data];
    
    NSString *strData = [NSObject dataToHexString:data];
    
    if ([strData hasPrefix:@"0102"] && [RSSI integerValue] < 0){
        if (![self.searchDeviceArrays containsObject:peripheral] && self.searchDeviceArrays.count <50) {
            
            [self.searchDeviceArrays addObject:peripheral];
            
            [self.RSSIDictionary setValue:[RSSI stringValue]
                                   forKey:[peripheral.identifier UUIDString]];
            
            [self.MACDictionary setValue:data forKey:[peripheral.identifier UUIDString]];
            NSString *macAddress = [self getMacAddressFromData:data];
            if ([macAddress isEqualToString:self.connectedMacAddress]) {
                self.discoverBlock(peripheral);
            }
            
            if([RSSI integerValue] >=-40){
                [self stopScanForPer];
            }
        }
    }
}
-(NSString *)getMacAddressFromData:(NSData *)data {
    NSData *macdata = [data subdataWithRange:NSMakeRange(2, 6)];
    NSMutableString *allMac = [NSMutableString string];
    
    for (int i = 0; i < macdata.length; i++) {
        NSData *dt = [macdata subdataWithRange:NSMakeRange(i, 1)];
        NSString *str = [[NSObject dataToHexString:dt]uppercaseString];
        if (i == 0) {
            [allMac appendString:str];
        }else{
            [allMac appendString:@":"];
            [allMac appendString:str];
        }
    }
    return allMac;
}
-(void)getstopPointMac:(NSData *)data{
    int time = [self getMaxTime:data];
    
    NSMutableString *mac = [NSMutableString string];
    
    for (int i = 0; i < 6; i++){
        NSData *subdata = [data subdataWithRange:NSMakeRange(2+i, 1)];
        int value = [self intForData:subdata];
        
        int macvalue = value ^ time;
        //NSLog(@"mac.value :%d",macvalue); // 22:22:19:28:37:46
        
        NSString *str = [NSString stringWithFormat:@"%x",macvalue];
        if (i == 0) {
            [mac appendString:str];
        }else{
            [mac appendString:@":"];
            [mac appendString:str];
        }
    }
//    NSLog(@"mac :%@",mac);
}

-(int)getMaxTime:(NSData *)data{
    
    int maxvalue = 0;
    for (int i = 0; i < 4; i++) {
        
        NSData *subdata = [data subdataWithRange:NSMakeRange(8+i, 1)];
        int value = [self intForData:subdata];
        maxvalue = MAX(maxvalue, value);
    }
    if (maxvalue == 0xFF) {
        maxvalue = 0xAA;
    }
    if (maxvalue == 0x00) {
        maxvalue = 0x55;
    }
    return maxvalue;
}

//连接外围设备
- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral{
    
    [self insertORdeleteDevice:peripheral isInsert:YES];
    self.connectBlock(peripheral);
    
    peripheral.delegate=self;//这个方法调用发现服务协议
    [peripheral discoverServices:nil];
}

//取消连接
-(void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error{
    
//    NSLog(@"central :%@",central);
//    NSLog(@"peripheral :%@",peripheral);
//    NSLog(@"error :%@",error);
    
    [self insertORdeleteDevice:peripheral isInsert:NO];
    self.disconnectBlock(peripheral);
}

//连接中断
- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error{
    
//    NSLog(@"device.state====3:%ld",peripheral.state);
    
    [self insertORdeleteDevice:peripheral isInsert:NO];
    self.disconnectBlock(peripheral);
}

-(void)insertORdeleteDevice:(CBPeripheral *)p isInsert:(BOOL)insert{
    if (insert) { // 新怎连接设备
        if (![self.connectArrays containsObject:p]) {
            [self.connectArrays addObject:p];
        }
    }
    if (!insert) {
        if ([self.connectArrays containsObject:p]) {
            [self.connectArrays removeObject:p];
        }
    }
}
#pragma mark - ====== peripheral methods and delegate ======发现服务
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error{
    for (CBService *service in [peripheral services]){
        [peripheral discoverCharacteristics:charauuids forService:service];
//        NSLog(@"service.uuid :%@",service.UUID);
    }
}

//发现特性
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    
    for (CBCharacteristic *character in [service characteristics]) {
//        NSLog(@"chara.uuid:%@",character.UUID);
        
        if ([character.UUID isEqual:[CBUUID UUIDWithString:kReaduuid]]) {
            [peripheral setNotifyValue:YES forCharacteristic:character];
        }
    }
}

//读取指定特性的数据
- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    self.notificationBlock(characteristic.value);
}


#pragma mark - 实例化中央服务
+(BleAPI *)Singleton:(NSString *)macAddress {
    @synchronized(api) {
        if (!api) {
            api=[[BleAPI alloc]initWithcentralManager:macAddress];
        }
        return api;
    }
}

- (id)initWithcentralManager:(NSString *)macAddress {
    
    self = [super init];
    if (self){
        self.connectedMacAddress = macAddress;
        if (!self.centralManager){
            
            self.centralManager=[[CBCentralManager alloc]initWithDelegate:self queue:dispatch_get_main_queue()];
            
//            NSTimer *timer= [NSTimer scheduledTimerWithTimeInterval:7 target:self selector:@selector(scanForPer) userInfo:nil repeats:YES];
//            [[NSRunLoop currentRunLoop]addTimer:timer forMode:NSRunLoopCommonModes];
        }
    }
    return self;
}

-(NSMutableArray *)connectArrays {
    if (!_connectArrays) {
        _connectArrays = [[NSMutableArray alloc]init];
    }
    return _connectArrays;
}

-(NSMutableArray *)searchDeviceArrays {
    if (!_searchDeviceArrays) {
        _searchDeviceArrays = [[NSMutableArray alloc]init];
    }
    return _searchDeviceArrays;
}

-(NSMutableDictionary *)RSSIDictionary{
    if (!_RSSIDictionary) {
        _RSSIDictionary = [[NSMutableDictionary alloc]init];
    }
    return _RSSIDictionary;
}
-(NSMutableDictionary *)MACDictionary{
    if (!_MACDictionary) {
        _MACDictionary = [[NSMutableDictionary alloc]init];
    }
    return _MACDictionary;
}
@end
