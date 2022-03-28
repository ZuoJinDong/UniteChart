package com.zjd.unite.chart.constant

/**
 * @author ZJD
 * @date 2021/6/30
 * @desc
 **/
object QuoteConstant {

    const val DEFAULT_VALUE = "---"

    const val DEFAULT_PRICE = "0.00"

    /** 商品ID */
    const val KEY_COLUMN_ID = 1 //商品ID

    /** 小数点位数 */
    const val KEY_COLUMN_DEC_POINT = 2 //小数点位数

    /** 板块ID */
    const val KEY_COLUMN_BOARD_ID = 3
    /** 商品状态 开闭市 0闭市1开市 */
    const val KEY_CONUMN_STATUS = 4
    /** 合约手数 */
    const val KEY_CONUMN_CONTRACT_SIZE = 5
    /** 夜盘 */
    const val KEY_COLUMN_NIGHT = 6
    /** 主力 */
    const val KEY_COLUMN_ZL = 7
    /** tag标签 */
    const val KEY_COLUMN_TAG = 8

    const val COLUMN_TYPE_ORDER = 0
    const val COLUMN_TYPE_CUSTOM_TAG_ORDER = 999
    const val CONUMN_TYPE_INDEX = 1001

    /** 商品名 */
    const val KEY_COLUMN_NAME = 1002 //商品名称

    /** 商品Code代码 */
    const val KEY_COLUMN_CODE = 1003 //代码名称

    /** 报价时间 */
    const val CONUMN_TYPE_TIME = 1004

    /** 现价 */
    const val KEY_COLUMN_PRICE = 1005 //最新价格

    const val CONUMN_TYPE_VOL = 1006 //现手


    /** 买入价 */
    const val CONUMN_TYPE_BUY_PRICE = 1007

    /** 卖出价 */
    const val CONUMN_TYPE_SELL_PRICE = 1008

    /** 开盘价 */
    const val CONUMN_TYPE_OPEN_PRICE = 1009

    /** 最高价 */
    const val CONUMN_TYPE_HIGH_PRICE = 1010

    /** 最低价 */
    const val CONUMN_TYPE_LOW_PRICE = 1011

    /** 昨收价 */
    const val KEY_COLUMN_YESTERDAY_PRICE = 1012

    /** 价格涨跌值 */
    const val KEY_COLUMN_RAISE = 1013

    /** 价格涨跌幅，是一个小数 */
    const val KEY_COLUMN_RAISE_PERCENT = 1014
    const val CONUMN_TYPE_AMPLITUDE = 1015
    const val CONUMN_TYPE_TOTAL_VOL = 1016 //成交量

    const val CONUMN_TYPE_AMOUT = 1017 //成交总额

    const val CONUMN_TYPE_HOLDING_DIFFERENT = 1018 //仓差

    const val CONUMN_TYPE_DIRECT = 1019
    const val CONUMN_TYPE_BUY_VOL = 1020 //买入量

    const val CONUMN_TYPE_SELL_VOL = 1021 //卖出量

    const val CONUMN_TYPE_HOLDING = 1022 //持仓

    const val CONUMN_TYPE_SETTLEMENTPRICE = 1023
    const val CONUMN_TYPE_NOW_AMOUT = 1024 //现额

    const val CONUMN_TYPE_RECOMMEND = 1025
    const val CONUMN_TYPE_FAST_RAISE = 1026
    const val CONUMN_TYPE_TURNOVER_RATE = 1027
    const val CONUMN_TYPE_P_OR_E_RATIO = 1028
    const val CONUMN_TYPE_LAST1 = 1029
    const val CONUMN_TYPE_LAST2 = 1030
    const val CONUMN_TYPE_LAST3 = 1031
    const val CONUMN_TYPE_LAST4 = 1032
    const val CONUMN_TYPE_LAST5 = 1033
    const val CONUMN_TYPE_BUY1 = 1034
    const val CONUMN_TYPE_BUY2 = 1035
    const val CONUMN_TYPE_BUY3 = 1036
    const val CONUMN_TYPE_BUY4 = 1037
    const val CONUMN_TYPE_BUY5 = 1038
    const val CONUMN_TYPE_BUYVOL1 = 1039
    const val CONUMN_TYPE_BUYVOL2 = 1040
    const val CONUMN_TYPE_BUYVOL3 = 1041
    const val CONUMN_TYPE_BUYVOL4 = 1042
    const val CONUMN_TYPE_BUYVOL5 = 1043
    const val CONUMN_TYPE_SELL1 = 1044
    const val CONUMN_TYPE_SELL2 = 1045
    const val CONUMN_TYPE_SELL3 = 1046
    const val CONUMN_TYPE_SELL4 = 1047
    const val CONUMN_TYPE_SELL5 = 1048
    const val CONUMN_TYPE_SELLVOL1 = 1049
    const val CONUMN_TYPE_SELLVOL2 = 1050
    const val CONUMN_TYPE_SELLVOL3 = 1051
    const val CONUMN_TYPE_SELLVOL4 = 1052
    const val CONUMN_TYPE_SELLVOL5 = 1053
    const val CONUMN_TYPE_VOLUME_RATIO = 1054
    const val CONUMN_TYPE_OUTER = 1055
    const val CONUMN_TYPE_INSIDE = 1056
    const val CONUMN_TYPE_AVG_PRICE = 1057 //均价

    const val CONUMN_TYPE_BUY_OR_SELL = 1058
    const val CONUMN_TYPE_BUY_OR_SELL_VOL = 1059
    const val CONUMN_TYPE_WEICHA = 1060
    const val CONUMN_TYPE_BUYPENDING_RATE = 1061
    const val CONUMN_TYPE_BUY_AMOUNT = 1062
    const val CONUMN_TYPE_SEL_AMOUNT = 1063
    const val CONUMN_TYPE_TOTAL_BUY_VOL = 1064
    const val CONUMN_TYPE_TOTAL_SELL_VOL = 1065
    const val CONUMN_TYPE_PRE_HOLDING = 1066
    const val CONUMN_TYPE_MIDDLE_PRICE = 1067
    const val CONUMN_TYPE_SYMBOL_OWNER = 1068
    const val CONUMN_TYPE_OUTER_AMOUNT = 1069
    const val CONUMN_TYPE_INSIDE_AMOUNT = 1070
    const val CONUMN_TYPE_LAST_TIME1 = 1071
    const val CONUMN_TYPE_LAST_TIME2 = 1072
    const val CONUMN_TYPE_LAST_TIME3 = 1073
    const val CONUMN_TYPE_LAST_TIME4 = 1074
    const val CONUMN_TYPE_LAST_TIME5 = 1075
    const val CONUMN_TYPE_LAST_VOL1 = 1076
    const val CONUMN_TYPE_LAST_VOL2 = 1077
    const val CONUMN_TYPE_LAST_VOL3 = 1078
    const val CONUMN_TYPE_LAST_VOL4 = 1079
    const val CONUMN_TYPE_LAST_VOL5 = 1080
    const val CONUMN_TYPE_LAST_TYPE1 = 1081
    const val CONUMN_TYPE_LAST_TYPE2 = 1082
    const val CONUMN_TYPE_LAST_TYPE3 = 1083
    const val CONUMN_TYPE_LAST_TYPE4 = 1084
    const val CONUMN_TYPE_LAST_TYPE5 = 1085
    const val CONUMN_TYPE_LINGXIAN = 1086


    /** 空数据 */
    const val CONUMN_TYPE_EMPTY = 10000

    /** 振幅 */
    const val CONUMN_TYPE_ZHENFU = 10001

    /** 点差 */
    const val CONUMN_TYPE_DIANCHA = 10002


    /** 昨结价 */
    const val KEY_COLUMN_YESTERDAY_SETTLEMENT = 1087 //昨结

    const val CONUMN_TYPE_FIVE_MIN_RAISE = 1088
    const val CONUMN_TYPE_RAISE_CHANGE_PERCENT = 1089
    const val CONUMN_TYPE_RAISE_CHANGE_PERCENT_REPEAT = 1090
    const val CONUMN_TYPE_QUOTE_UNIT = 1091
    const val WARN_TYPE_UNUSUAL_QUOTE_PRICE = 1092
    const val CONUMN_TYPE_HIGH_LIMIT_PRICE = 1093 //涨停

    const val CONUMN_TYPE_LOW_LIMIT_PRICE = 1094 //跌停价

    const val CONUMN_TYPE_TODAY_POSITION_INCREASE = 1095 //日增仓


    /** 图表类型：空  */
    const val PERIOD_NONE = -2

    /** 图表类型：分时图  */
    const val PERIOD_TS = -1

    /** 图表类型：日k  */
    const val PERIOD_DAY = 1

    /** 图表类型：5分  */
    const val PERIOD_5_MINUTE = 2

    /** 图表类型：15分  */
    const val PERIOD_15_MINUTE = 3

    /** 图表类型：30分  */
    const val PERIOD_30_MINUTE = 4

    /** 图表类型：60分  */
    const val PERIOD_60_MINUTE = 5

    /** 图表类型：4时  */
    const val PERIOD_4_HOUR = 6

    /** 图表类型：2日  */
    const val PERIOD_2_DAY = 7

    /** 图表类型：3日  */
    const val PERIOD_3_DAY = 11

    /** 图表类型：5日  */
    const val PERIOD_5_DAY = 15

    /** 图表类型：闪电图  */
    const val PERIOD_FLASH = 8

    /** 图表类型：周k  */
    const val PERIOD_WEEK = 9

    /** 图表类型：月k  */
    const val PERIOD_MONTH = 10

    /** 图表类型：1分  */
    const val PERIOD_1_MINUTE = 12

    /** 图表类型：季k  */
    const val PERIOD_SEASON = 13

    /** 图表类型：年k  */
    const val PERIOD_YEAR = 14

    /* VIP功能 */
    /* VIP功能 */
    /** 图表类型：3分  */
    const val PERIOD_3_MINUTE = 16

    /** 图表类型：10分  */
    const val PERIOD_10_MINUTE = 17

    /** 图表类型：20分  */
    const val PERIOD_20_MINUTE = 18

    /** 图表类型：2时  */
    const val PERIOD_2_HOUR = 19

    /** 图表类型：3时  */
    const val PERIOD_3_HOUR = 20

    /** 图表类型：6时  */
    const val PERIOD_6_HOUR = 21

    /** 图表类型：8时  */
    const val PERIOD_8_HOUR = 22

    /** 图表类型：12时  */
    const val PERIOD_12_HOUR = 23
    /** 图表类型：分钟 */
    const val PERIOD_MINUTE = 101
    /** 图表类型：更多 */
    const val PERIOD_MORE = 102
    /** 图表类型：编辑*/
    const val PERIOD_EDIT = 103
    /**  */
    const val min_time = 1000 * 60L
    /**  */
    const val hour_time = 1000 * 60 * 60L
    /**  */
    const val day_time = 1000 * 60 * 60 * 24L


    const val CONUMN_TYPE_BUY6 = 1096
    const val CONUMN_TYPE_BUY7 = 1097
    const val CONUMN_TYPE_BUY8 = 1098
    const val CONUMN_TYPE_BUY9 = 1099
    const val CONUMN_TYPE_BUY10 = 1100
    const val CONUMN_TYPE_BUYVOL6 = 1101
    const val CONUMN_TYPE_BUYVOL7 = 1102
    const val CONUMN_TYPE_BUYVOL8 = 1103
    const val CONUMN_TYPE_BUYVOL9 = 1104
    const val CONUMN_TYPE_BUYVOL10 = 1105
    const val CONUMN_TYPE_SELL6 = 1106
    const val CONUMN_TYPE_SELL7 = 1107
    const val CONUMN_TYPE_SELL8 = 1108
    const val CONUMN_TYPE_SELL9 = 1109
    const val CONUMN_TYPE_SELL10 = 1110
    const val CONUMN_TYPE_SELLVOL6 = 1111
    const val CONUMN_TYPE_SELLVOL7 = 1112
    const val CONUMN_TYPE_SELLVOL8 = 1113
    const val CONUMN_TYPE_SELLVOL9 = 1114
    const val CONUMN_TYPE_SELLVOL10 = 1115

    //增仓手数
    const val CONUMN_ADD_VOL = 1116

    //开平标记：1-双开  2-双平 3-空开 4-多开 5-空平 6-多平 7-多换 8-空换 9-换手
    const val CONUMN_OPEN_CLOSE = 1117

}