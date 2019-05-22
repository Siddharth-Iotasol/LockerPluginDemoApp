//
//  BleAPI.h
//  keyPad
//
//  Created by HAOZO.MAC on 15/10/16.
//  Copyright © 2015年 HAOZO.MAC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

#define Servicesuuids [CBUUID UUIDWithString:@"FEE7"]
#define Servicesuuids2 [CBUUID UUIDWithString:@"FBCA"]

#define charauuids nil

#define kReaduuid      @"36F6"
#define kWriteuuid      @"36F5"

typedef void(^bleAPIdiscoverBlock)(CBPeripheral *p);   // 发现设备
typedef void(^bleAPIconnectBlock)(CBPeripheral *p);    // 链接设备
typedef void(^bleAPIdisconnectBlock)(CBPeripheral *p); // 断开连接
typedef void(^bleAPInotificationBlock)(NSData *data);  // 监听数据
typedef void(^bleAPStopScanBlock)(void);               // 停止扫描


@interface BleAPI : NSObject<CBCentralManagerDelegate,CBPeripheralDelegate>

@property(nonatomic,strong)CBCentralManager *centralManager;//中央服务
@property(nonatomic,strong)NSMutableArray *searchDeviceArrays;//发现外围设备数组
@property(strong,nonatomic)NSMutableArray *connectArrays; // 链接成功的设备


@property(strong,nonatomic)NSMutableDictionary *RSSIDictionary;
@property(strong,nonatomic)NSMutableDictionary *MACDictionary;

@property(nonatomic,strong)bleAPIdiscoverBlock discoverBlock;
@property(nonatomic,strong)bleAPIconnectBlock connectBlock;
@property(nonatomic,strong)bleAPIdisconnectBlock disconnectBlock;
@property(nonatomic,strong)bleAPInotificationBlock notificationBlock;
@property(nonatomic,strong)bleAPStopScanBlock stopScanBlock;

@property(nonatomic,strong)NSString *connectedMacAddress;
@property(nonatomic,strong)CBPeripheral *connectedPeriferal;

-(void)bleAPIBlocks:(bleAPIdiscoverBlock)_discover // 发现设备回调
   withconnectBlock:(bleAPIconnectBlock)_connect // 链接设备回调
withdisconnectBlock:(bleAPIdisconnectBlock)_disconnect // 断开链接回调
withnotificationBlock:(bleAPInotificationBlock)_notification // notify回调
  withstopScanBlock:(bleAPStopScanBlock)_stopScan; // 停止扫描回调

+(BleAPI *)Singleton:(NSString *)macAddress;
-(void)scanForPer;
-(void)stopScanForPer;

-(void)writeData:(NSData *)data forcharauuid:(NSString *)charauuid;
#pragma mark - 连接，断开操作  连接
-(void)didConnection:(NSArray *)devices;
-(void)didDisconnection:(NSArray *)devices;//断开连接


@end
