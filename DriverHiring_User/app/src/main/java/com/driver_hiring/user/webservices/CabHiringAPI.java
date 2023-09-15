/* JSON API for android appliation */
package com.driver_hiring.user.webservices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CabHiringAPI {
    private final String urlString = "http://adriver.hostoise.com/Handler1.ashx";

    private static String convertStreamToUTF8String(InputStream stream) throws IOException {
	    String result = "";
	    StringBuilder sb = new StringBuilder();
	    try {
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[4096];
            int readedChars = 0;
            while (readedChars != -1) {
                readedChars = reader.read(buffer);
                if (readedChars > 0)
                   sb.append(buffer, 0, readedChars);
            }
            result = sb.toString();
		} catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }


    private String load(String contents) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(60000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        OutputStreamWriter w = new OutputStreamWriter(conn.getOutputStream());
        w.write(contents);
        w.flush();
        InputStream istream = conn.getInputStream();
        String result = convertStreamToUTF8String(istream);
        return result;
    }


    private Object mapObject(Object o) {
		Object finalValue = null;
		if (o.getClass() == String.class) {
			finalValue = o;
		}
		else if (Number.class.isInstance(o)) {
			finalValue = String.valueOf(o);
		} else if (Date.class.isInstance(o)) {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", new Locale("en", "USA"));
			finalValue = sdf.format((Date)o);
		}
		else if (Collection.class.isInstance(o)) {
			Collection<?> col = (Collection<?>) o;
			JSONArray jarray = new JSONArray();
			for (Object item : col) {
				jarray.put(mapObject(item));
			}
			finalValue = jarray;
		} else {
			Map<String, Object> map = new HashMap<String, Object>();
			Method[] methods = o.getClass().getMethods();
			for (Method method : methods) {
				if (method.getDeclaringClass() == o.getClass()
						&& method.getModifiers() == Modifier.PUBLIC
						&& method.getName().startsWith("get")) {
					String key = method.getName().substring(3);
					try {
						Object obj = method.invoke(o, null);
						Object value = mapObject(obj);
						map.put(key, value);
						finalValue = new JSONObject(map);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
		return finalValue;
	}

    public JSONObject Dregister(String photo,String name,String dob,String gender,String email,String contact,String totexp,String address,String city,String state,String pincode,String hourprice,String pass) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "Dregister");
        p.put("photo",mapObject(photo));
        p.put("name",mapObject(name));
        p.put("dob",mapObject(dob));
        p.put("gender",mapObject(gender));
        p.put("email",mapObject(email));
        p.put("contact",mapObject(contact));
        p.put("totexp",mapObject(totexp));
        p.put("address",mapObject(address));
        p.put("city",mapObject(city));
        p.put("state",mapObject(state));
        p.put("pincode",mapObject(pincode));
        p.put("hourprice",mapObject(hourprice));
        p.put("pass",mapObject(pass));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject Dlogin(String Email,String pass) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "Dlogin");
        p.put("Email",mapObject(Email));
        p.put("pass",mapObject(pass));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DcheckProfile(String did) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DcheckProfile");
        p.put("did",mapObject(did));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DgetProfile(String did) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DgetProfile");
        p.put("did",mapObject(did));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DgetExperience(String did) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DgetExperience");
        p.put("did",mapObject(did));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DgetTrips(String did) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DgetTrips");
        p.put("did",mapObject(did));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DgetDocuments(String did) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DgetDocuments");
        p.put("did",mapObject(did));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DupdateProfile(String did,String photo,String name,String dob,String gender,String email,String contact,String totexp,String address,String city,String state,String pincode,String hourprice) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DupdateProfile");
        p.put("did",mapObject(did));
        p.put("photo",mapObject(photo));
        p.put("name",mapObject(name));
        p.put("dob",mapObject(dob));
        p.put("gender",mapObject(gender));
        p.put("email",mapObject(email));
        p.put("contact",mapObject(contact));
        p.put("totexp",mapObject(totexp));
        p.put("address",mapObject(address));
        p.put("city",mapObject(city));
        p.put("state",mapObject(state));
        p.put("pincode",mapObject(pincode));
        p.put("hourprice",mapObject(hourprice));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DaddExperience(String did,ArrayList<String> cartype,ArrayList<String> months) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DaddExperience");
        p.put("did",mapObject(did));
        p.put("cartype",mapObject(cartype));
        p.put("months",mapObject(months));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DaddTrips(String did,ArrayList<String> ptype,ArrayList<String> trips) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DaddTrips");
        p.put("did",mapObject(did));
        p.put("ptype",mapObject(ptype));
        p.put("trips",mapObject(trips));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DaddDocs(String did,String doctype,String dpath) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DaddDocs");
        p.put("did",mapObject(did));
        p.put("doctype",mapObject(doctype));
        p.put("dpath",mapObject(dpath));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DaddLocation(String did,String latlng,String date,String time) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DaddLocation");
        p.put("did",mapObject(did));
        p.put("latlng",mapObject(latlng));
        p.put("date",mapObject(date));
        p.put("time",mapObject(time));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DgetRides(String did,String date,String src) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DgetRides");
        p.put("did",mapObject(did));
        p.put("date",mapObject(date));
        p.put("src",mapObject(src));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DgetCarDetails(String cid) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DgetCarDetails");
        p.put("cid",mapObject(cid));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DgetTransactions(String did) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DgetTransactions");
        p.put("did",mapObject(did));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject DchangeStatus(String rid,String status,String src) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "DchangeStatus");
        p.put("rid",mapObject(rid));
        p.put("status",mapObject(status));
        p.put("src",mapObject(src));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject getNotification(String uid) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "getNotification");
        p.put("uid",mapObject(uid));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject Uregister(String name,String dob,String gender,String email,String contact,String address,String city,String state,String pincode,String pass) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "Uregister");
        p.put("name",mapObject(name));
        p.put("dob",mapObject(dob));
        p.put("gender",mapObject(gender));
        p.put("email",mapObject(email));
        p.put("contact",mapObject(contact));
        p.put("address",mapObject(address));
        p.put("city",mapObject(city));
        p.put("state",mapObject(state));
        p.put("pincode",mapObject(pincode));
        p.put("pass",mapObject(pass));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject Ulogin(String Email,String pass) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "Ulogin");
        p.put("Email",mapObject(Email));
        p.put("pass",mapObject(pass));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UgetProfile(String uid) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UgetProfile");
        p.put("uid",mapObject(uid));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UupdateProfile(String uid,String name,String dob,String gender,String email,String contact,String address,String city,String state,String pincode) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UupdateProfile");
        p.put("uid",mapObject(uid));
        p.put("name",mapObject(name));
        p.put("dob",mapObject(dob));
        p.put("gender",mapObject(gender));
        p.put("email",mapObject(email));
        p.put("contact",mapObject(contact));
        p.put("address",mapObject(address));
        p.put("city",mapObject(city));
        p.put("state",mapObject(state));
        p.put("pincode",mapObject(pincode));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UgetCar(String uid) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UgetCar");
        p.put("uid",mapObject(uid));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UaddCar(String uid,String brand,String model,String transmission,String year,String chasisno,String carno,String type,String fuel) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UaddCar");
        p.put("uid",mapObject(uid));
        p.put("brand",mapObject(brand));
        p.put("model",mapObject(model));
        p.put("transmission",mapObject(transmission));
        p.put("year",mapObject(year));
        p.put("chasisno",mapObject(chasisno));
        p.put("carno",mapObject(carno));
        p.put("type",mapObject(type));
        p.put("fuel",mapObject(fuel));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UdeleteCar(String cid) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UdeleteCar");
        p.put("cid",mapObject(cid));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UsearchDrivers(String uid,String cartype,ArrayList<String> ptype,String totdays) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UsearchDrivers");
        p.put("uid",mapObject(uid));
        p.put("cartype",mapObject(cartype));
        p.put("ptype",mapObject(ptype));
        p.put("totdays",mapObject(totdays));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UaddRide(String uid,String did,String cid,String sdate,String stime,String sadd,String slatlng,String edate,String etime,String eadd,String elatlng,String totdays,String tothours,String ptype,String Bdatetime,String price) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UaddRide");
        p.put("uid",mapObject(uid));
        p.put("did",mapObject(did));
        p.put("cid",mapObject(cid));
        p.put("sdate",mapObject(sdate));
        p.put("stime",mapObject(stime));
        p.put("sadd",mapObject(sadd));
        p.put("slatlng",mapObject(slatlng));
        p.put("edate",mapObject(edate));
        p.put("etime",mapObject(etime));
        p.put("eadd",mapObject(eadd));
        p.put("elatlng",mapObject(elatlng));
        p.put("totdays",mapObject(totdays));
        p.put("tothours",mapObject(tothours));
        p.put("ptype",mapObject(ptype));
        p.put("Bdatetime",mapObject(Bdatetime));
        p.put("price",mapObject(price));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UgetRides(String uid,String date,String src) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UgetRides");
        p.put("uid",mapObject(uid));
        p.put("date",mapObject(date));
        p.put("src",mapObject(src));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UgetDriverLocation(String rid) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UgetDriverLocation");
        p.put("rid",mapObject(rid));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UrateRider(String rid,String rate) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UrateRider");
        p.put("rid",mapObject(rid));
        p.put("rate",mapObject(rate));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UgetTransactions(String uid) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","RestAPI");
        o.put("method", "UgetTransactions");
        p.put("uid",mapObject(uid));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

}


