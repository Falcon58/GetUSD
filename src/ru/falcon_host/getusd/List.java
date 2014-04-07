package ru.falcon_host.getusd;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class List extends Activity {
	
	TextView text_out;
	String [] Dates;
	double [] Prices;
	int len;
	SharedPreferences sPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		text_out = (TextView)findViewById(R.id.textView2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
	}
	public void refresh(View v)
	{
		text_out.setText("");
		try
		{
			Calendar cal = Calendar.getInstance();
			cal.setLenient(false);
			SimpleDateFormat dFormat = new SimpleDateFormat("dd/MM/yyyy");
			String cDate = dFormat.format(cal.getTime());
			cal.add(Calendar.DAY_OF_MONTH, -7);
			String pDate = dFormat.format(cal.getTime());
			
			URL url = new URL("http://www.cbr.ru/scripts/XML_dynamic.asp?date_req1=" + pDate + "&date_req2=" + cDate + "&VAL_NM_RQ=R01235");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(url.openStream()));
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("Record");
			len = nodeList.getLength();
			Dates = new String [len];
			Prices = new double [len];
			for (int i = 0; i < len; i++)
			{
				Node node = nodeList.item(i);
				NamedNodeMap attributes = node.getAttributes();
				Node Date  = attributes.getNamedItem("Date");
				Dates[i] = Date.getNodeValue();
				Element element = (Element)nodeList.item(i);
				NodeList Value = element.getElementsByTagName("Value");
				Element price = (Element)Value.item(0);
				String vPrice = price.getTextContent();
				vPrice = vPrice.replace(",", ".");
				Prices[i] = Double.parseDouble(vPrice);
			}
			for(int i = 0; i < len - 1; i++)
			{
				for(int j = 0; j < len - i - 1; j++)
				{
					if(Prices[j] > Prices[j + 1])
					{
						double Temp = Prices[j];
						Prices[j] = Prices[j + 1];
						Prices[j + 1] = Temp;
						
						String Temp2 = Dates[j];
						Dates[j] = Dates[j + 1];
						Dates[j] = Temp2;
					}
				}
			}
			for(int i = 0; i < len; i++)
			{
				text_out.append(Dates[i] + " - " + Prices[i] + "\n");
			}
		} catch (Exception e)
		{
			text_out.setText("Error!");
		}
	}
	public void display(View v)
	{
		Intent intent = new Intent (this, Average.class);
		intent.putExtra("PR", Prices);
		startActivity(intent);
	}
	public void onStop() {
		super.onStop();
		sPref = getSharedPreferences("myPreferens", MODE_PRIVATE);
		Editor ed = sPref.edit();
		ed.putInt("length", len);
		for(int i = 0; i < len; i++)
		{
			ed.putString("Str_" + i, Dates[i]);
			ed.putString("P_" + i, Double.toString(Prices[i]));
		}
		ed.commit();
	}
	public void onStart() {
		super.onStop();
		sPref = getSharedPreferences("myPreferens", MODE_PRIVATE);
		len = sPref.getInt("length", 0);
		if(len != 0)
		{
			text_out.setText("");
			Dates = new String [len];
			Prices = new double [len];
			for(int i = 0; i < len; i++)
			{
				Dates[i] = sPref.getString("Str_" + i, "");
				Prices[i] = Double.parseDouble(sPref.getString("P_" + i, ""));
			}
			for(int i = 0; i < len; i++)
			{
				text_out.append(Dates[i] + " - " + Prices[i] + "\n");
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
