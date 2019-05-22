//
//  NSObject+plistEx.h
//  phonecases
//
//  Created by HAOZO.MAC on 15/8/3.
//  Copyright (c) 2015年 HAOZO.MAC. All rights reserved.
//

#import <Foundation/Foundation.h>

#define devicePlist [NSHomeDirectory() stringByAppendingPathComponent:@"/Documents/BowLight.plist"]

#define debugFile [NSHomeDirectory() stringByAppendingPathComponent:@"/Documents/debug.txt"]

@interface NSObject (plistEx)


+(void)createFile;
-(NSMutableString *)readDebug;
-(void)writeDebug:(NSString *)content;




+ (NSString *)DPLocalizedString:(NSString *)translation_key;

+(NSString*)dataToHexString:(NSData*)data;// 十六进制转换成字符串
-(NSData*)convert:(NSString*)str;//字符串转换十六进制   比如 FF ->0XFF
-(int)intForData:(NSData *)data;//十六进制转换十进制  比如 0x37 -> 55
@end
