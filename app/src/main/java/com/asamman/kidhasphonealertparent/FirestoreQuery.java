package com.asamman.kidhasphonealertparent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FirestoreQuery {

    // Function to generate Firestore query JSON dynamically based on an array of kid names
    public static String generateQueryJson(String[] kidNames) throws JSONException {
        JSONObject structuredQuery = new JSONObject();

        // Set the 'from' array
        JSONArray fromArray = new JSONArray();
        JSONObject fromObject = new JSONObject();
        fromObject.put("collectionId", "alerts");
        fromObject.put("allDescendants", false);
        fromArray.put(fromObject);
        structuredQuery.put("from", fromArray);

        // Set the 'where' object
        JSONObject whereObject = new JSONObject();

        // Set the 'compositeFilter'
        JSONObject compositeFilter = new JSONObject();
        compositeFilter.put("op", "OR");

        // Create an array to hold the 'filters' objects
        JSONArray filtersArray = new JSONArray();

        // Create 'fieldFilter' objects for each kid name
        for (String kidName : kidNames) {
            JSONObject fieldFilter = new JSONObject();

            // Set the 'fieldFilter' object
            JSONObject fieldFilterObj = new JSONObject();
            fieldFilterObj.put("op", "EQUAL");

            JSONObject field = new JSONObject();
            field.put("fieldPath", "kid_name");
            fieldFilterObj.put("field", field);

            JSONObject value = new JSONObject();
            value.put("stringValue", kidName);
            fieldFilterObj.put("value", value);

            // Set the 'fieldFilter' within 'fieldFilter'
            fieldFilter.put("fieldFilter", fieldFilterObj);

            // Add the 'fieldFilter' object to the filters array
            filtersArray.put(fieldFilter);
        }

        // Add the filters array to the 'compositeFilter'
        compositeFilter.put("filters", filtersArray);

        // Add the 'compositeFilter' to the 'where' object
        whereObject.put("compositeFilter", compositeFilter);

        // Add the 'where' object to the structured query
        structuredQuery.put("where", whereObject);

        JSONObject output = new JSONObject().put("structuredQuery", structuredQuery);
        return output.toString();
    }

}
