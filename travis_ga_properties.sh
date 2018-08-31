DIR="app/src/playstore/res/xml"
GA_FILE="$DIR/global_tracker.xml"
mkdir -p $DIR && touch $GA_FILE
echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>" > $GA_FILE
echo "<resources>" >> $GA_FILE
echo "	<string name=\"ga_trackingId\" translatable=\"false\">nothing</string>" >> $GA_FILE
echo "</resources>" >> $GA_FILE



