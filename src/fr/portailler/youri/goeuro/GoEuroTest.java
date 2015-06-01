package fr.portailler.youri.goeuro;

import java.io.IOException;
import java.io.Closeable;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.csvreader.CsvWriter;



public class GoEuroTest {

	public static void main(String[] args) {
		
		if(args.length < 1){
			System.out.println("Error : GoEuroTest requires a STRING parameter ");
			System.out.println("Syntax : java -jar GoEuroTest.jar [String]");
			System.exit(0);
		}
		
		JSONArray jsonArray = null;
		
		try{			
			URL url = new URL("http://api.goeuro.com/api/v2/position/suggest/en/" + args[0]);
			String result = getResultFromEndpoint(url);
			jsonArray = new JSONArray(result);
		}
		catch(UnknownHostException uhe){			
			System.out.println("The host : " + uhe.getMessage() + " is unreachable.");
			System.exit(0);
		}
		catch(IOException ioe){
			System.out.println("The string parameter \"" + args[0] + "\" is invalid, make sure it does not contain any special characters such as '.' or '/'.");			
			System.exit(0);
		}		
		catch(Exception e){
			System.out.println("Error:");
			e.printStackTrace();
			System.exit(0);
		}
		
		writeJSONArrayInCsv(args[0],jsonArray);
		
			
	}
	
	private static String getResultFromEndpoint(URL url) throws IOException{
		
		Scanner scanner = null;
		InputStream is = null;
		
		try{
			is = url.openStream();
			scanner = new Scanner(is);
			StringBuilder sb = new StringBuilder();
		    while (scanner.hasNext())
		        sb.append(scanner.nextLine());
		    return sb.toString();
		}
		catch(IOException ioe){			
			throw ioe;
		}
		finally{
			closeStream(scanner);
			closeStream(is);
		}
	}
	
	private static void writeJSONArrayInCsv(String usrParam, JSONArray jsonArray){
		
		String[] csvColumnNames = {"_id","name","type","latitude","longitude"};
		CsvWriter writer = null;
		
		
		try{
			writer = new CsvWriter(generateCsvFilename(usrParam));
			/* writes header */
			writer.writeRecord(csvColumnNames);
			
			/* for every JSON object in the JSON array, writes a line*/
			for(int i=0; i<jsonArray.length(); i++)
				writer.writeRecord(getJSONObjectValues(jsonArray.getJSONObject(i), csvColumnNames));			
		
		}
		catch(JSONException jsonException){
			System.out.println("An error occured while parsing the result JSON");
			jsonException.printStackTrace();
		}
		catch(IOException ioe){
			System.out.println("An error occured while writing the file");
			ioe.printStackTrace();
		}
		finally{
			if(writer != null)
				writer.close();			
		}
	}
	
	/* putting the date and hour in the name of the file prevents overwriting previous query results, and file name conflicts */
	private static String generateCsvFilename(String usrParam){
		SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmmss");
		return usrParam.replaceAll("[^a-zA-Z0-9\\._]+", "_") + sdf.format(new Date()) + ".csv";
	}
	
	private static String[] getJSONObjectValues(JSONObject jsonObject, String[] columnNames){
		
		String[] result = new String[5];
		
		for(int i=0; i<3; i++)
			result[i] = jsonObject.get(columnNames[i]).toString();
		for(int i=3; i<5; i++)
			result[i] = jsonObject.getJSONObject("geo_position").get(columnNames[i]).toString();
				
		return result;
	}
	
	private static void closeStream(Closeable c){
		try{
			if(c != null)
				c.close();
		}
		catch(IOException ioe){
			
		}
	}

}
