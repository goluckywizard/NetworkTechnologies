import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NetworkInteraction {
    static final String GraphHopper_API_key = "e65d22b7-9d07-45f9-a024-3ef238517a7b";
    static final String OpenTripMap_API_key = "5ae2e3f221c38a28845f05b6db9064405b5fb485153159316afe3b22";
    static final String OpenWeather_API_jey = "15b7ccb823b8736bd2b2bc3f64d54922";
    HttpClient client;

    public JsonArray getVariants(String place) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(("https://graphhopper.com/api/1/geocode?q=" + place + "&locale=en&debug=true&key=" + GraphHopper_API_key)))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //JsonParser parser = new JsonParser();
        JsonObject head =  JsonParser.parseString(response.body()).getAsJsonObject();
        return head.get("hits").getAsJsonArray();
    }
    public Place[] getListOfPlaces(Point x) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://api.opentripmap.com/0.1/ru/places/radius?" +
                        "radius=40000&lon="+x.lon+"&lat="+x.lat+"&format=json&limit=4&apikey="+OpenTripMap_API_key))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //JsonParser parser = new JsonParser();
        //JsonObject head =  JsonParser.parseString(response.body()).getAsJsonArray();
        //System.out.println(head.toString());
        JsonArray list = JsonParser.parseString(response.body()).getAsJsonArray();
        Place[] placesList = new Place[list.size()];
        for (int i = 0; i < list.size(); i++) {
            placesList[i] = new Place();
            JsonObject cur = list.get(i).getAsJsonObject();
            placesList[i].setName(cur.get("name").getAsString());
            placesList[i].setXid(cur.get("xid").getAsString());
        }
        return placesList;
        /*for (int i = 0; i < placesList.length; i++) {
            System.out.println(placesList[i].toString());
        }*/
    }
    public void getDescriptionByXid(Place x) throws IOException, InterruptedException {
        //System.out.println(x.xid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://api.opentripmap.com/0.1/ru/places/xid/"+x.xid+"?apikey=" + OpenTripMap_API_key))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject head = JsonParser.parseString(response.body()).getAsJsonObject();
        System.out.println(head.toString());
        if (head == null)
            return;
        if (head.get("wikipedia_extracts") == null)
            return;
        if (head.get("wikipedia_extracts").getAsJsonObject().get("text") == null)
            return;
        x.setDescription(head.get("wikipedia_extracts").getAsJsonObject().get("text").getAsString());
    }
    public String getWeather(Point x) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openweathermap.org/data/2.5/weather?lat="+x.lat+"&lon="
                        +x.lon+"&units=metric&lang=ru&appid=" + OpenWeather_API_jey))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject head = JsonParser.parseString(response.body()).getAsJsonObject();
        System.out.println(head.toString());
        String res = "";
        res += "Погода: " + head.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString() + "\n";
        res += "Температура: " + head.get("main").getAsJsonObject().get("temp").getAsString() + "\n";
        return res;
    }
    public NetworkInteraction() {
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

    }
}
