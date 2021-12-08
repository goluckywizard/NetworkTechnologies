public class Point {
    float lat, lon;
    String name;

    public Point(float lat, float lon, String name) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Имя: " + name
                + ", долгота: " + lat
                + ", широта: " + lon;
    }
}