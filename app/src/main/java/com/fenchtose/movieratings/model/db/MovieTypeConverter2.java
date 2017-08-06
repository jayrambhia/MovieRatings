package com.fenchtose.movieratings.model.db;

import android.arch.persistence.room.TypeConverter;

import com.fenchtose.movieratings.model.Rating;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;

public class MovieTypeConverter2 {

	public static Gson gson = new Gson();

	@TypeConverter
	public static ArrayList<Rating> fromString(String data) {
		JsonArray json = gson.fromJson(data, JsonElement.class).getAsJsonArray();
		ArrayList<Rating> ratings = new ArrayList<>();
		for (int i=0; i<json.size(); i++) {
			ratings.add(new Rating(json.get(i).getAsJsonObject().get("Source").getAsString(),
					json.get(i).getAsJsonObject().get("Value").getAsString()));
		}
		return ratings;
	}

	@TypeConverter
	public static String listToString(ArrayList<Rating> ratings) {
		JsonArray json = new JsonArray();
		for (Rating rating : ratings) {
			JsonObject item = new JsonObject();
			item.addProperty("Source", rating.getSource());
			item.addProperty("Value", rating.getValue());
			json.add(item);
		}

		return json.toString();
	}

	@TypeConverter
	public static Date fromLong(long data) {
		return new Date(data);
	}

	@TypeConverter
	public static long fromDate(Date date) {
		return date.getTime();
	}
}
