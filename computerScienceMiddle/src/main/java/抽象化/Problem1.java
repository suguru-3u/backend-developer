package 抽象化;

/**
 * 緯度経度の計算
 *
 * <p>
 * ある都市の緯度 latitude、経度 longitude が与えられるので、
 * 赤道（equator）、本初子午線（prime meridian）を基準として、
 * その都市が東西南北どこに位置しているかを文字列で返す、calculateLocation という関数を作成してください。
 * 文字列は「緯度/経度」の順で表記してください。
 * <p>
 *
 * 入力のデータ型： double-float latitude, double-float longitude
 * 出力のデータ型： string
 *
 * calculateLocation(77.147489,0) --> north/prime meridian
 * calculateLocation(-55.78774,0) --> south/prime meridian
 * calculateLocation(-36.615626,68.130625) --> south/east
 * calculateLocation(9.236204,-25.806614) --> north/west
 * calculateLocation(-29.998979,-19.74947) --> south/west
 * calculateLocation(0,0) --> equator/prime meridian
 */

class Problem1{
    public static String calculateLocation(double latitude, double longitude){
        String latitudeDirection = getLatitudeDirection(latitude);
        String longitudeDirection = getLongitudeDirection(longitude);
        return latitudeDirection + "/" + longitudeDirection;
    }

    private static String getLatitudeDirection(double latitude){
        if(latitude == 0){
            return "equator";
        }
        if(latitude > 0) {
            return "north";
        }
        return "south";
    }

    private static String getLongitudeDirection(double longitude) {
        if(longitude == 0){
            return "prime meridian";
        }
        if(longitude > 0) {
            return "east";
        }
        return "west";
    }
}

