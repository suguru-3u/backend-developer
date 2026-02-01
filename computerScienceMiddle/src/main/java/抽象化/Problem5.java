package 抽象化;

import java.util.Objects;

/**
 * 物体の自由落下
 *
 * <p>
 * 重力加速度 g[m/s2] で、高さ h[m] から物体をそっと落下させ、地上に到達するまで t 秒を要するとき、
 * h = 1/2 gt2 が成り立つとします。
 * 惑星 planet、落下時間 time[s] が与えられるので、落下距離[mile]を返す、
 * fallingDistance という関数を作成してください。各惑星の重力加速度は以下の表を参考にしてください。
 * <p>
 * 惑星名	重力加速度[m/s2]
 * Earth（地球）	9.8
 * Jupiter（木星）	24.79
 * Mercury（水星）	3.7
 * その他	0
 * メートルからマイルへの変換は、1[m] = 0.000621371[mile] を使用してください。
 * また、求めた落下距離[mile]の小数点以下は切り捨ててください。
 * <p>
 * <p>
 * 入力のデータ型： string planet, integer time
 * 出力のデータ型： integer
 * <p>
 * fallingDistance("Earth",5000) --> 76117
 * fallingDistance("Jupiter",1000) --> 7701
 * fallingDistance("Mercury",4500) --> 23278
 * fallingDistance("Pluto",2000) --> 0
 */

class Problem5 {
    public static int fallingDistance(String planet, int time) {
        double planetGravity = planetGravityMpss(planet);
        double height = planetGravity * Math.pow(time, 2) / 2;
        return (int) Math.floor(height * 0.000621371);
    }

    public static double planetGravityMpss(String planet) {
        if (Objects.equals(planet, "Earth")) {
            return 9.8;
        }

        if (Objects.equals(planet, "Jupiter")) {
            return 24.79;
        }

        if (Objects.equals(planet, "Mercury")) {
            return 3.7;
        }

        return 0;
    }
}

