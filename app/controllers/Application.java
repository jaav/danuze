package controllers;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.model.*;
import models.Tweet;
import models.TweetMessage;
import models.UploadForm;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.h2.util.IOUtils;
import play.*;
import play.data.Form;
import play.libs.Json;
import play.modules.aws.dynamodb.DynamoDB;
import play.mvc.*;

import scalax.io.support.FileUtils;
import views.html.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Application extends Controller {

  final static Form<TweetMessage> tweetForm = form(TweetMessage.class);

  public static Result index() {
      Tweet tweet = new Tweet();
      tweet.setTweet("a first tweet");
      Set<String> hashtags = new HashSet<>();
      hashtags.add("one");
      hashtags.add("two");
      tweet.setHashtags(hashtags);
      //yyyy_MM_dd_hh_mm_tweetorder
      DateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_");
      Date now = new Date();
      Calendar cal = new GregorianCalendar();
      cal.setTime(now);
      int minutes = cal.get(Calendar.MINUTE);
      int minute_key = minutes/15;
      String key = sdf.format(new Date());
      tweet.setId(key+minute_key+"_1");
      Tweet tweet2 = new Tweet();
      tweet2.setTweet("a second tweet");
      Set<String> hashtags2 = new HashSet<>();
      hashtags2.add("three");
      hashtags2.add("four");
      tweet2.setHashtags(hashtags2);
      tweet2.setId(key+minute_key+"_2");

      tweet.save();
      tweet2.save();

      DynamoDBMapper mapper = DynamoDB.mapper();
      Tweet loadedTweet = mapper.load(Tweet.class, "2012_12_03_02_58_1");
    return ok(index.render("Your new application is ready."));
  }

    public static Result showForm(){
        TweetMessage t = new TweetMessage();
        return ok(uploadForm.render(tweetForm));
    }

    public static Result uploadTweet(){

        Tweet tweet = null;
        Form<TweetMessage> tweetMessage = tweetForm.bindFromRequest();
        String tweetKey = getTweetKey();


        List<Tweet> tweets = new ArrayList<Tweet>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            tweets = mapper.readValue(tweetMessage.get().jsonTweet, new TypeReference<List<Tweet>>() { });
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        int counter = 1;
        for (Tweet aTweet : tweets) {
            aTweet.setId(tweetKey);
            aTweet.setSequence(counter);
            aTweet.save();
            counter++;
        }
        DynamoDBMapper dynamoMapper = DynamoDB.mapper();



        /*Condition rangeKeyCondition = new Condition()
        .withComparisonOperator(ComparisonOperator.GT.toString()) .withAttributeValueList(new AttributeValue().withS(twoWeeksAgoStr));*/
        /*QueryRequest queryRequest = new QueryRequest().withTableName(table
            .withHashKeyValue(new AttributeValue().withS(replyId))
            .withRangeKeyCondition(rangeKeyCondition)
            .withAttributesToGet(Arrays.asList("Message", "ReplyDateTime",
                    .withLimit(1).withExclusiveStartKey(lastKeyEvaluated);
               QueryResult result = client.query(queryRequest);*/






        AttributeValue value = new AttributeValue(tweetKey);
        DynamoDBQueryExpression query = new DynamoDBQueryExpression(value);
        //Tweet result = dynamoMapper.query(Tweet.class, query);
        List<Tweet> currentTweets = dynamoMapper.query(Tweet.class, query);

        Http.MultipartFormData body = request().body().asMultipartFormData();
        List<Http.MultipartFormData.FilePart> images = body.getFiles();
        for (Http.MultipartFormData.FilePart image : images) {
            String ext = FilenameUtils.getExtension(image.getFilename());
            Tweet correspondingTweet = getCorrespondingTweet(tweets, image.getFilename());
            if(correspondingTweet!=null)
                image.getFile().renameTo(new File("/Users/jefw/Sites/wanuus/images", new StringBuilder(correspondingTweet.getId())
                    .append("_")
                    .append(correspondingTweet.getSequence())
                    .append(".")
                    .append(ext)
                    .toString()));
        }




            /*try {
                IOUtils.copy(new FileInputStream(picture.getFile()), new FileOutputStream(new File("/Users/jefw/Drop", picture.getFilename())));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }*/

        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("message", "Upload successful");
        return ok(result);
    }

    private static String getTweetKey(){
        DateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_");
        Date now = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(now);
        int minutes = cal.get(Calendar.MINUTE);
        String minute_key = "Q"+(1+minutes/15);
        String key = sdf.format(new Date());
        return key+minute_key;
    }

    private static Tweet getCorrespondingTweet(List<Tweet> tweets, String imageName){
        for (Tweet tweet : tweets) {
            if(tweet.getImage()!=null && tweet.getImage().indexOf(imageName)>=0) return tweet;
        }
        return null;
    }
  
}