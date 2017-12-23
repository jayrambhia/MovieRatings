# Flutter - Movie Ratings
![status](https://travis-ci.org/jayrambhia/MovieRatings.svg?branch=master)

<p align="center">
<img src="https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/cover.png"/>
</p>

## Note
Flutter has been **unpublished** from Google Playstore. It uses accessibility service and recent Playstore policy suggests that `Apps requesting accessibility services should only be used to help users with disabilities use Android devices and apps`. So I have decided to remove it from the playstore. I am planning to add Flutter to other app stores such as F-Droid.

You can get the latest Playstore version here - [0.1.4](https://github.com/jayrambhia/MovieRatings/releases/download/v0.1.4/flutter_v0.1.4.apk)

## Playstore description

Flutter is a smart app that helps you decide what to watch on streaming apps like Netflix 🎥! It shows movie ratings ⭐ on your screen making sure that you don't even have to search 🔎 for it. It's ad-free and promotion-free.

<b>What does Flutter do?</b>
 
Some of the video streaming apps do not show you ratings of the feature that you want to see, be it a movie, a documentary or a TV show. You would probably search it on Google or Imdb to make sure that it's good. Flutter makes your life easier by showing you the movie ratings directly on your screen without typing a single letter.
 
<b>How does Flutter work?</b>
 
Flutter is smart and lightweight. It uses Android's accessibility feature 📋 to get movie names from streaming apps. It looks up the movie on the internet 🌐, gets basic details and accurate ratings from Open Movie Database, and shows you the ratings on your screen instantly. Whenever you open a page of a movie or a TV show, you'll see the ratings on the bottom-right side of your screen.
 
Flutter is cool 😎. It doesn't ask for your contacts or personal details, bother you with push notifications. Flutter is active only when you're browsing the content on video streaming apps and it will sleep peacefully the rest of the time.
 
<b>How to make it work?</b>
 
Flutter is brilliant but you're the boss! You need to enable Flutter's accessibility in Accessibility settings on your phone. But there's an easier way. Open the app, click the button and it will open up settings for you. Enable it. Sit back, relax and enjoy watching your movies!
 
<b>Apps that Flutter supports?</b>
 - Netflix
 - Support for other popular streaming apps coming soon!
 
<b>What’s the future of Flutter?</b>

 - Support for <b>Android TV</b>!
 - Support for more streaming apps - so that whatever you use, Flutter will be there to help you out with movie ratings!
 - We want Flutter to be your go-to pal for anything related to movies, documentaries and TV shows from ratings to recommendations.
 
<b>Concerns?</b>

Flutter uses Open Movie Database to get movie ratings so it is possible that it might not have the ratings of some not so popular movies.
Open Movie Database has ratings gathered from various popular websites like Imdb.
Flutter is not endorsed by or affiliated with Imdb.com, Netflix.com or omdbapi.com

-----

## Contribute

Contributions are welcome!

To build the project, create `local.properties` in the base directory if it does not exist and add following line to it.

```
omdb_api_key=<API_KEY>
```

You can get your API key here - [OMDB API Key](http://www.omdbapi.com/apikey.aspx)

If you don't have an API key, you can use `omdb_api_key=""` and the project will use `PreloadedMovieProvider` in debug build. It will return search results for `thor` and `batman` only.
