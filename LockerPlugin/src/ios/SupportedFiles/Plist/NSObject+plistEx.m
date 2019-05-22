//
//  NSObject+plistEx.m
//  phonecases
//
//  Created by HAOZO.MAC on 15/8/3.
//  Copyright (c) 2015年 HAOZO.MAC. All rights reserved.
//

#import "NSObject+plistEx.h"

#define C2I(c) ((c >= '0' && c<='9') ? (c-'0') : ((c >= 'a' && c <= 'z') ? (c - 'a' + 10): ((c >= 'A' && c <= 'Z')?(c - 'A' + 10):(-1))))

#define CURR_LANG ([[NSLocale preferredLanguages] objectAtIndex:0])

@implementation NSObject (plistEx)

+ (NSString *)DPLocalizedString:(NSString *)translation_key {
    
    NSString * s = NSLocalizedString(translation_key, nil);// 当前语言如果不是中文，默认采用英文
    if (![CURR_LANG isEqualToString:@"zh-Hans-CN"] && ![CURR_LANG isEqualToString:@"zh-Hans"] && ![CURR_LANG isEqualToString:@"zh-Hans-US"]) {
        
        NSString * path = [[NSBundle mainBundle] pathForResource:@"en" ofType:@"lproj"];
        NSBundle * languageBundle = [NSBundle bundleWithPath:path];
        s = [languageBundle localizedStringForKey:translation_key value:@"" table:nil];
    }
    return s;
}

/*********************************/

+(void)createFile{
    if (![[NSFileManager defaultManager]fileExistsAtPath:debugFile]){
        NSString *str = @" \n";
        [str writeToFile:debugFile atomically:YES encoding:NSUTF8StringEncoding error:nil];
    }
}

-(NSMutableString *)readDebug{
    NSMutableString *debug = [NSMutableString stringWithContentsOfFile:debugFile
                                                              encoding:NSUTF8StringEncoding
                                                                 error:nil];
    return debug;
}

-(void)writeDebug:(NSString *)content {
    if (content == nil) {
        return;
    }
    NSMutableString *all = [self readDebug];
    [all appendString:content];
    [all appendString:@"\n"];
    [all writeToFile:debugFile atomically:YES encoding:NSUTF8StringEncoding error:nil];
}
/*********************************/






// 十六进制转换成字符串
+(NSString*)dataToHexString:(NSData*)data {
    if (data == nil) {
        return @"";
    }
    Byte *dateByte = (Byte *)[data bytes];
    
    NSString *hexStr=@"";
    for(int i=0;i<[data length];i++) {
        NSString *newHexStr = [NSString stringWithFormat:@"%x",dateByte[i]&0xff]; ///16进制数
        if([newHexStr length]==1)
            hexStr = [NSString stringWithFormat:@"%@0%@",hexStr,newHexStr];
        else
            hexStr = [NSString stringWithFormat:@"%@%@",hexStr,newHexStr];
    }
    return hexStr;
}

//字符串转换十六进制   比如 FF ->0XFF
-(NSData*)convert:(NSString*)str{
    const char* cs = str.UTF8String;
    int count = (int)strlen(cs);
    int8_t  bytes[count / 2];
    for(int i = 0; i<count; i+=2){
        char c1 = *(cs + i);
        char c2 = *(cs + i + 1);
        if(C2I(c1) >= 0 && C2I(c2) >= 0){
            bytes[i / 2] = C2I(c1) * 16 + C2I(c2);
            
        }else{
            return nil;
        }
    }
    return [NSData dataWithBytes:bytes length:count /2];
}

//十六进制转换十进制  比如 0x37 -> 55
-(int)intForData:(NSData *)data{
    uint8_t* a = (uint8_t*) [data bytes];
    NSString *str=[NSString stringWithFormat:@"%d",*a];
    return [str intValue];
}
@end
