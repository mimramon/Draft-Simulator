import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.imageio.ImageIO;

class RequestHandler
{
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(10)).build();

    public static String GetCardText(String _cardName)
    {
        //the get request
        String uri = "https://api.scryfall.com/cards/named?exact=" + URLEncoder.encode(_cardName, StandardCharsets.UTF_8) + "&format=text";
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(uri)).build();
        //string from the request
        System.out.println("try fetch card text");
        HttpResponse<String> response = null;
        do
        {
        	try
        	{
        		response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        	}
        	catch(Exception ex)
        	{
        		System.out.println("error fetching card text: " + ex);
        		response = null;
        	}
        } 
        while(response == null);
        System.out.println("got card text");
        return response.body();
    }

    public static BufferedImage GetCardImage(String _cardName)
    {
        //this is the get request
        String urlString = "https://api.scryfall.com/cards/named?exact=" + URLEncoder.encode(_cardName, StandardCharsets.UTF_8) + "&format=image&version=small";
        BufferedImage image = null;
        do 
        {
        	try
        	{
        		URL url = new URL(urlString);
        		image = ImageIO.read(url);
        	}
        	catch(IOException ex)
        	{
        		System.out.println("error fetching card image: " + ex);
        		image = null;
        	}
        }
        while(image == null);
        try {Thread.sleep(50);}
        catch(Exception ex) {System.out.println("thread couldnt sleep: " + ex);}
        return image;
    }
}