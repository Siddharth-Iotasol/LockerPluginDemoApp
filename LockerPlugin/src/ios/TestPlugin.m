//
//  TestPlugin.m
//  bleDemo
//
//  Created by Akash on 07/03/19.
//  Copyright © 2019 Akash. All rights reserved.
//

/********* TestPlugin.m Cordova Plugin Implementation *******/

#import "TestPlugin.h"
#import <Cordova/CDV.h>

@interface TestPlugin()

@property (strong, nonatomic)NSMutableArray *arr;

@property(strong,nonatomic)NSData *tokenData;
@property(strong,nonatomic)CBPeripheral *connectDevice;

@property(assign, nonatomic)NSInteger openCount;
@property(assign, nonatomic)NSInteger closeCount;

@end

@implementation TestPlugin

- (void)coolMethod:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];
    
    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)showAlert:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];
    
    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)initialize:(CDVInvokedUrlCommand*)command {
    // inititalize Ble and scan for services
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];
    
    if (echo != nil && [echo length] > 0) {
        [self bleAPIInit:echo];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}
- (void)openLock:(CDVInvokedUrlCommand*)command {
    //write data for openlock
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];
    
    if (echo != nil && [echo length] > 0) {
        [self btnUnlockPressed];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}
- (void)closeLock:(CDVInvokedUrlCommand*)command {
    //write data for close lock
    //write data for openlock
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];
    
    if (echo != nil && [echo length] > 0) {
        [self btnLockPressed];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}
- (void)btnLockPressed {
    if (!self.tokenData) {
        return;
    }
    [self writeData:[self getCloseLockData]];
}
- (void)btnUnlockPressed {
    if (!self.tokenData) {
        return;
    }
    [self openLock];
    //    [self writeData:[self getWorkStatu]];
}

#pragma mark - All bluetooth devices
-(void)bleAPIInit:(NSString *)macAddress {
    self.bleApi = [BleAPI Singleton:macAddress];
    
    //__block typeof(self) weakSelf = self;
    __weak TestPlugin *weakSelf = self;
    [self.bleApi bleAPIBlocks:^(CBPeripheral *p) { // 发现设备
        [weakSelf foundDevice:p];
    } withconnectBlock:^(CBPeripheral *p) { // 链接设备
        [weakSelf connectDevice:p];
    } withdisconnectBlock:^(CBPeripheral *p) { // 断开链接
        [weakSelf disConnectDevice:p];
    } withnotificationBlock:^(NSData *data) { // 监听获取数据
        [weakSelf notificationData:data];
    } withstopScanBlock:^{ // 停止扫描
        [weakSelf stopScan];
    }];
}
-(void)foundDevice:(CBPeripheral *)p {
    self.connectDevice = nil;
    if (p != nil) {
        NSLog(@"Found Device: %@ ",p);
        self.connectDevice = p;
        if (!self.connectDevice) {
            //self.errLb.text = [NSString stringWithFormat:@"%@",LDevicemiss];
            return;
        }
        //[self writeControlDebug:@"开始链接设备"];
        [self.bleApi didConnection:@[self.connectDevice]];
        //[self readDeviceMsg:self.connectDevice.name mac:nil connect:Lconnected];
        
    } else {
        NSLog(@"Not Found any devices");
    }
}
-(void)connectDevice:(CBPeripheral *)p {
    NSLog(@"connected Device:%@",p);
    [self performSelector:@selector(writeData:) withObject:[self getTokenSignal] afterDelay:2];
}
-(void)disConnectDevice:(CBPeripheral *)p {
    
}

-(void)stopScan {
    
}


#pragma mark - To Write BLE Device

#pragma mark - 数据的发送
-(NSData*) getTokenSignal { //获取token数据
    
    // len:16  imei  or mac
    Byte signal[] = {0x06,0x01,0x01,0x01,/*
                                          arc4random()%255,arc4random()%255,arc4random()%255,arc4random()%255,
                                          arc4random()%255,arc4random()%255,arc4random()%255,arc4random()%255,
                                          arc4random()%255,arc4random()%255,arc4random()%255,arc4random()%255*/
        
        0x45,0x38,0x38,0x49,0x41,0x5F,0x4A,0x78,0x41,0x3A,0x4A,0x3F}; // 06010101
    Byte encryptSignal[16];
    AES128_ECB_encrypt(signal, aeskey, encryptSignal);//AES 128加密
    NSData *encryptData = [NSData dataWithBytes:&encryptSignal length:sizeof(encryptSignal)];
    //    NSData *encryptData = [NSData dataWithBytes:&signal length:sizeof(signal)];
    
    return encryptData;
}

// 获取电量
-(void)getPower:(NSData *)token{
    Byte *tokenByte = (Byte *)[token bytes];
    Byte signal[16] = {0x02,0x01,0x01,0x01,
        tokenByte[0],tokenByte[1],tokenByte[2],tokenByte[3],
        0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
    
    Byte encryptSignal[16];
    AES128_ECB_encrypt(signal, aeskey, encryptSignal);//AES 128加密
    //    NSData *encryptData = [NSData dataWithBytes:&signal length:sizeof(signal)];
    NSData *encryptData = [NSData dataWithBytes:&encryptSignal length:sizeof(encryptSignal)];
    [self writeData:encryptData];
}

// 发送数据
-(void)writeData:(NSData *)data{
    NSLog(@"发送数据 :%@",data);
    [self.bleApi writeData:data forcharauuid:kWriteuuid];
}

// 查询锁的工作状态
-(NSData *)getWorkStatu {
    Byte *tokenByte = (Byte *)[self.tokenData bytes];
    
    Byte signal[16] = {
        0x05,0x0E,0x01,0x01,tokenByte[0],tokenByte[1],tokenByte[2],tokenByte[3],
        0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
    
    Byte encryptSignal[16];
    AES128_ECB_encrypt(signal, aeskey, encryptSignal);//AES 128加密
    NSData *encryptData = [NSData dataWithBytes:&encryptSignal length:sizeof(encryptSignal)];
    return encryptData;
}

-(void)openLock {
    [self writeData:[self getOpenLockData:@"000000"]];
    //    [self writeData:[self getOpenLockData:@"123456"]];
}



-(void)notificationData:(NSData *)data {
    
    Byte *signalByte = (Byte *)[data bytes];
    Byte decryptSignal[16];
    AES128_ECB_decrypt(signalByte, aeskey, decryptSignal);//AES 128解密
    
    NSData *d2 = [NSData dataWithBytes:decryptSignal length:16];
    NSString *desryptStr = [NSObject dataToHexString:d2];
    
    //self.errLb.text = @"";
    
    NSLog(@"收到解析数据 :%@",desryptStr);
    
    if ([desryptStr hasPrefix:@"0602"]) {
        
        //[self writeControlDebug:@"获取token成功，开始获取电量"];
        self.tokenData = [d2 subdataWithRange:NSMakeRange(3, 4)];
        [self performSelector:@selector(getPower:) withObject:self.tokenData afterDelay:1];
        
        
        int v1 = [self intForData:[d2 subdataWithRange:NSMakeRange(8, 1)]];
        int v2 = [self intForData:[d2 subdataWithRange:NSMakeRange(9, 1)]];
        // NSString *version = [NSString stringWithFormat:@"%@:v%d.%d",LbluetoothVer,v1,v2];
        // self.verLb.text = version;
    }
    
    if ([desryptStr hasPrefix:@"05080100"]) {
        //[self writeControlDebug:@"复位/关锁成功"];
        //self.openstatuLb.text = [NSString stringWithFormat:@"%@:%@",Lnowstatus,Lreset];
        
        self.closeCount++;
        //self.closeCountLb.text = [NSString stringWithFormat:@"%@: %ld",LresetCount,self.closeCount];
        if (true) { //self.mySwitch.on // 自动开锁
            [self performSelector:@selector(openLock) withObject:nil afterDelay:2];
        }
        
        //self.stateImage.image = [UIImage imageNamed:@"off"];
    }
    
    if ([desryptStr hasPrefix:@"05080101"]) {
        //[self writeControlDebug:@"复位/关锁超时"];
        //self.errLb.text = [NSString stringWithFormat:@"%@:%@%@，%@",Lnowstatus,Lreset,Lfailed,Ltimeout];
    }
    
    
    if ([desryptStr hasPrefix:@"05020100"]) {
        //[self writeControlDebug:@"开锁成功"];
        //self.openstatuLb.text = [NSString stringWithFormat:@"%@:%@",Lnowstatus,LstatusUnlock];
        self.openCount++;
        //self.openCountLb.text = [NSString stringWithFormat:@"%@: %ld",LunlockCount,self.openCount];
        //self.stateImage.image = [UIImage imageNamed:@"on"];
    }
    
    if ([desryptStr hasPrefix:@"05020101"]) {
        //[self writeControlDebug:@"开锁失败"];
        //self.errLb.text = [NSString stringWithFormat:@"%@%@",Lunlock,Lfailed];
    }
    if ([desryptStr hasPrefix:@"05020102"]) {
        //[self writeControlDebug:@"开锁失败"];
        //self.errLb.text = [NSString stringWithFormat:@"%@%@",Lunlock,Lerror];
    }
    
    if ([desryptStr hasPrefix:@"050d0100"]) {
        
        // [self writeControlDebug:@"复位/关锁成功"];
        //self.stateImage.image = [UIImage imageNamed:@"off"];
        //self.openstatuLb.text = [NSString stringWithFormat:@"%@:%@",Lnowstatus,Lreset];
        
        self.closeCount++;
        //self.closeCountLb.text = [NSString stringWithFormat:@"%@: %ld",LresetCount,self.closeCount];
    }
    
    if ([desryptStr hasPrefix:@"050d0101"]) {
        //[self writeControlDebug:@"复位/关锁失败"];
        //self.errLb.text = [NSString stringWithFormat:@"%@%@",Lreset,Lfailed];
    }
    if ([desryptStr hasPrefix:@"050d0102"]) {
        //[self writeControlDebug:@"复位/关锁失败"];
        //self.errLb.text = [NSString stringWithFormat:@"%@%@",Lreset,Lerror];
    }
    
    if ([desryptStr hasPrefix:@"020201"]) { // 获取电量
        
        //[self writeControlDebug:@"成功获取电量"];
        if ([desryptStr hasPrefix:@"020201ff"]){
            //self.errLb.text = [NSString stringWithFormat:@"%@",LpowError];
        }else{
            NSString *powStr = [NSString stringWithFormat:@"%d%@",[NSObject intForData:[d2 subdataWithRange:NSMakeRange(3, 1)]],@"%"];
            //[self.powerBt setTitle:powStr forState:0];
        }
    }
}

#pragma mark - 发送指令的拼接
/*************************************************************/

// 开锁
-(NSData *)getOpenLockData:(NSString *)pwd{
    
    //[self writeControlDebug:@"开锁"];
    Byte *tokenByte = (Byte *)[self.tokenData bytes];
    
    NSData *pwdData = [pwd dataUsingEncoding:NSUTF8StringEncoding];
    Byte *pwdByte = (Byte *)[pwdData bytes];
    Byte signal[16] = {
        0x05,0x01,0x06,pwdByte[0],pwdByte[1],pwdByte[2],pwdByte[3],pwdByte[4],pwdByte[5],
        tokenByte[0],tokenByte[1],tokenByte[2],tokenByte[3],0x00,0x00,0x00
    };
    
    Byte encryptSignal[16];
    AES128_ECB_encrypt(signal, aeskey, encryptSignal); //AES 128加密
    NSData *encryptData = [NSData dataWithBytes:&encryptSignal length:sizeof(encryptSignal)];
    //    NSData *encryptData = [NSData dataWithBytes:&signal length:sizeof(signal)];
    return encryptData;
}
// 关锁
-(NSData *)getCloseLockData{
    //[self writeControlDebug:@"复位/关锁"];
    Byte *tokenByte = (Byte *)[self.tokenData bytes];
    Byte signal[16] = {
        0x05,0x0C,0x01,0x01,
        tokenByte[0],tokenByte[1],tokenByte[2],tokenByte[3],
        0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
    };
    
    Byte encryptSignal[16];
    AES128_ECB_encrypt(signal, aeskey, encryptSignal);//AES 128加密
    NSData *encryptData = [NSData dataWithBytes:&encryptSignal length:sizeof(encryptSignal)];
    return encryptData;
}


@end

