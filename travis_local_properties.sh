touch local.properties
echo "omdb_api_key=\"\"" > local.properties
echo "ratings_endpoint=\"\"" >> local.properties
echo "ratings_api_key=\"\"" >> local.properties

mkdir -p app/src/playstore/res/xml && touch app/src/playstpre/res/xml/global_tracker.xml
echo "<?xml version="1.0" encoding="utf-8"?>" > app/src/playstpre/res/xml/global_tracker.xml
echo "<resources>" >> app/src/playstpre/res/xml/global_tracker.xml
echo "	<string name=\"ga_trackingId\" translatable=\"false\">nothing</string>" >> app/src/playstpre/res/xml/global_tracker.xml
echo "</resources>" >> app/src/playstpre/res/xml/global_tracker.xml



