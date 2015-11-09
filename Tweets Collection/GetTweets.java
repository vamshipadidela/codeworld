

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Date;
import java.util.List;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.FilterQuery;

public class GetTweets {

	public static void main(String[] args) {
		
		 ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
	        configurationBuilder.setOAuthConsumerKey("dxwgdNrDJmAevgjXueRyA1u0E" )
	                .setOAuthConsumerSecret("vSzmU9J4UH8mpLT1CKhlf6sFa03iyPsMn6wVyrdSqq46hraQ3p")
	                .setOAuthAccessToken("362977080-8bNTZYbfTGVgIouWiszM1PIdih0O1gvXX9AIFnE5")
	                .setOAuthAccessTokenSecret("RAhOMcfXyIkATvV0cpj407cv5pumxDbeuLCEAnEQf7vBt").setJSONStoreEnabled(true);
        TwitterStream tweets = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        
        StatusListener statusListener=new StatusListener(){
        	 public void onException(Exception arg0) {
                 // TODO Auto-generated method stub

             }

             public void onDeletionNotice(StatusDeletionNotice arg0) {
                 // TODO Auto-generated method stub

             }

             public void onScrubGeo(long arg0, long arg1) {
                 // TODO Auto-generated method stub

             }

             public void onStatus(Status status) {
            	  String fileName;
            	 JSONObject list=new JSONObject();
            	 try{
            	  list.put("TweetId",status.getId());
            	  list.put("UserName",status.getUser().getScreenName());
            	  try{
            	  list.put("UserId",status.getUser().getId());
            	  }
            	  catch(Exception e)
            	  {
                	  list.put("UserId",JSONObject.NULL);
            	  }
            	  list.put("Language",status.getLang());
            	  list.put("Text",status.getText());
            	  try{
            	  list.put("Place",status.getPlace().getCountry());
            	  }
            	  catch(Exception e)
            	  {
            		  list.put("Place",JSONObject.NULL);
            	  }
            	  list.put("RetweetCount", status.getRetweetCount());
            	  list.put("FavoriteCount", status.getUser().getFavouritesCount());
            	  list.put("DateTime",status.getCreatedAt());
            	  list.put("HashTag",status.getHashtagEntities()); 
            	  languages lang= languages.valueOf(status.getLang());  
            	  switch(lang){
            	  case de:
            		  if(Globals.GermanTweetCount<250)
            		  {
            			  fileName= "F:\\IR\\GermanTweets250Mix15.txt";
                     		try {
                         	 FileWriter file = new FileWriter(fileName,true);
                         	 file.write(list.toString());
                         	 file.write(",\r\n");
                              file.flush();
                              file.close();
                          } catch (IOException e) {
                              e.printStackTrace();
                   
                          }
                     		fileName= "F:\\IR\\GermanTweetsAllJSON250Mix15.txt";
                     		try {
                         	 FileWriter file = new FileWriter(fileName,true);
                         	 String json = TwitterObjectFactory.getRawJSON(status);
                         	 file.write(json);
                         	 file.write(",\r\n");
                              file.flush();
                              file.close();
                          } catch (IOException e) {
                        	  
                              e.printStackTrace();
                   
                          }
                     		 /* WriteToFile(list,fileName);
                     		  */
            		  Globals.GermanTweetCount++;
            		  }
            		  
            		  else
            		  {
            			  /*System.exit(0);*/
            		  }
            		  break;
            		  
            	  case en:
            		  if(Globals.EnglishTweetCount<250)
            		  {
            		   fileName= "F:\\IR\\EnglishTweetsMix15.txt";
            		   try {
                         	 FileWriter file = new FileWriter(fileName,true);
                         	 file.write(list.toString());
                         	 file.write(",\r\n");
                              file.flush();
                              file.close();
                          } catch (IOException e) {
                              e.printStackTrace();
                   
                          }
            		   fileName= "F:\\IR\\EnglishTweetsAllJSONMix15.txt";
            		   try {
                      	 FileWriter file = new FileWriter(fileName,true);
                      	 String json = TwitterObjectFactory.getRawJSON(status);
                      	 file.write(json);
                      	 file.write(",\r\n");
                           file.flush();
                           file.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
            		 /* WriteToFile(list,fileName);*/
            		  Globals.EnglishTweetCount++;
            		  }
            		  else
            		  {
            			  System.exit(0);
            		  }
            		  break;
            	  
            	  case ru:
            		  if(Globals.RussianTweetCount<250)
            		  {
           		   fileName= "F:\\IR\\RussianTweets250Mix15.txt";
           		try {
               	 FileWriter file = new FileWriter(fileName,true);
               	 file.write(list.toString());
               	 file.write(",\r\n");
                    file.flush();
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
         
                }
           	 fileName= "F:\\IR\\parse.txt";
  		   try {
            	 FileWriter file = new FileWriter(fileName,true);
            	 String json = TwitterObjectFactory.getRawJSON(status);
            	 file.write(json);
            	 file.write(",\r\n");
                 file.flush();
                 file.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
           		 /* WriteToFile(list,fileName);
           		  */
           		  Globals.RussianTweetCount++;
            		  }
            		  else
            		  {
/*            			  System.exit(0);
*/            		  }
            	  break;
            	 }
            	  
            	 }
            	 catch(JSONException e)
            	 {
            		 
            	 }
                 
             }

             private void WriteToFile(JSONObject list, String fileName) {
				// TODO Auto-generated method stub
            	 /*try {
                	 FileWriter file = new FileWriter(fileName,true);
                	 file.write(list.toString());
                	 file.write(",\r\n");
                     file.flush();
                     file.close();
                 } catch (IOException e) {
                     e.printStackTrace();
          
                 }*/
			}

			public void onTrackLimitationNotice(int arg0) {
                 // TODO Auto-generated method stub

             }

			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}

         };
        FilterQuery queryTweets = new FilterQuery();
		/*String keywords[]= {"Ebola","Эбола"}; */
       /* String keywords[]= {"Ebola","Эбола","MERS","MEPC","Krebs","Рак","yoga","йога","самоубийство","Selbstmord"};
        */
        /*String keywords[]= {"MERS"};*/
         String keywords[]= {"Ebola","MERS","yoga","cancer"};   
		queryTweets.language(new String[]{"en"}).track(keywords);
		tweets.addListener(statusListener);
		tweets.filter(queryTweets);  
}
	public enum languages{
		en,
		de,
		ru		
	}
	public static class Globals{

	     public static int EnglishTweetCount = 0;
	       public static int RussianTweetCount = 0;
	        public static int GermanTweetCount = 0;
	}
}