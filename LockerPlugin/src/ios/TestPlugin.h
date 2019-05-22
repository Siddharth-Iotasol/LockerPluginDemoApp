//
//  TestPlugin.h
//  bleDemo
//
//  Created by Akash on 07/03/19.
//  Copyright © 2019 Akash. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import "PublicSet.h"

static uint8_t aeskey[16] = {58,96,67,42,92,01,33,31,41,30,15,78,12,19,40,37}; // 圆形锁
//static uint8_t aeskey[16] = {4,56,66,63,76,94,27,65,6,34,72,57,13,21,38,53}; // 测试

@interface TestPlugin : CDVPlugin
@property(nonatomic,strong)BleAPI *bleApi;

- (void)initialize:(CDVInvokedUrlCommand*)command;
- (void)openLock:(CDVInvokedUrlCommand*)command;
- (void)closeLock:(CDVInvokedUrlCommand*)command;
- (void)coolMethod:(CDVInvokedUrlCommand*)command;
- (void)showAlert:(CDVInvokedUrlCommand*)command;
@end
