# Flutter - Movie Ratings
![status](https://travis-ci.org/jayrambhia/MovieRatings.svg?branch=master)

<p align="center">
<img src="https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/cover.png" width="480px"/>
</p>

You can get the latest Playstore version here on [Playstore](https://play.google.com/store/apps/details?id=com.fenchtose.flutter&referrer=utm_source%3Dgithub) - or download directly - [0.3.3](https://github.com/jayrambhia/MovieRatings/releases/download/v0.3.3/flutter_v0.3.3.apk)

## Screenshots of master

Search Page | Favorites Page | Movie Page
:----------:|:--------------:|:----------: 
![](https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/search_screen.png) | ![](https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/likes_screen.png) | ![](https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/movie_screen.png)

Collections Page | Collection Page | Settings Page
:----------:|:--------------:|:---------------:
![](https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/collections_page.png) | ![](https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/collection_page.png) | ![](https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/settings_screen.png)

Series Page | List of Episodes on Series Page | Episode Page |
:----------:|:-------------------------------:|:-----------------------------:
![](https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/series_page.png) | ![](https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/series_episodes_page.png) | ![](https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/episodes_page.png)

## Contribute

Contributions are welcome!

To build the project, create `local.properties` in the base directory if it does not exist and add following line to it.

```
omdb_api_key=<API_KEY>
ratings_endpoint=<RATINGS_ENDPOINT>
ratings_api_key=<RATINGS_API_KEY>
```

You can get your API key here - [OMDB API Key](http://www.omdbapi.com/apikey.aspx)

If you don't have an API key, you can use `omdb_api_key=""` and the project will use `PreloadedMovieProvider` in debug build. It will return search results for `thor` and `batman` only.

`RATINGS_ENDPOINT` is Flutter's private server to obtain accurate ratings. You may replace it with your own server url. You may keep it as empty string and it will fallback to OMDB api.

`ratings_api_key` should be used as `ratings_api_key=""` as we're not giving out any keys at this moment.

## Playstore description

<p align="center">
<img src="https://github.com/jayrambhia/MovieRatings/blob/master/screenshots/playstore_logo.png" width="160px"/>
</p>

Flutter is a smart app that helps you decide what to watch on streaming apps like Netflix and Amazon Prime! It shows movie ratings on your screen making sure that you don't even have to search for it. It's completely ad-free and promotion-free.

<b>Accessibility services disclaimer</b>

This app uses Accessibility services. Flutter uses accessibility services to retrieve and read the window content to obtain name of the movies when you're using supported streaming apps. Once the title is obtained, the app will show its IMDb rating on your screen.

<b>What does Flutter do?</b>

Some of the video streaming apps do not show you ratings of the title that you want to see, be it a movie, a documentary or a TV show. The change in Netflix rating system caused an uproar in the online community. When you want to find out if a movie is good or bad before watching it on your streaming services, you would probably search it on Google or Imdb to find the ratings and reviews. Flutter makes your life easier by showing you the movie ratings directly on your screen without typing a single letter.
Flutter also offers a support for Text to speech and speaks out the movie rating which is very useful for someone with disabilities.

<b>Is that all Flutter does?</b>

That is the primary functionality of the app. But it has much more to offer. Flutter provides an in-built search to find new movies, TV shows to watch. You can create your own collections based on your personal choices, browse your history and keep a list of everything that you like. Keep up with the latest version to get the new features.

<b>How does Flutter work?</b>

Flutter is smart and lightweight (just over 2MB in size). It uses Android's accessibility feature  to get movie names from streaming apps like Netflix, BBC iPlayer, Jio TV, etc. It looks up the movie on the internet, gets basic details and accurate ratings from Open Movie Database, and shows you the ratings on your phone instantly without any hassles. Whenever you open a page of a movie or a TV show, you'll see the ratings on the bottom-right side of your screen.

This movie rating app doesn't ask for your contacts or personal details, bother you with push notifications. It’s active only when you're browsing the content on video streaming apps and it will sleep peacefully the rest of the time to save as much battery as possible. We care about your privacy and do not send your personal information to the cloud.

<b>How to make it work?</b>

Flutter is brilliant but you're the boss! You need to enable Flutter's accessibility in Accessibility settings on your phone. But there's an easier way. Open the app, click the button and it will open up settings for you. Enable it. Sit back, relax and enjoy watching your movies!

<b>Apps that Flutter supports?</b>
 - Netflix, Amazon Prime Video and Google Play Movies & TV for the worldwide audience.
 - BBC iPlayer for UK users.
 - Hotstar, Jio TV and Jio Cinema for our Indian users.
 - Support for other popular streaming apps coming soon!

<b>What’s the future of Flutter?</b>

 - Support for <b>Android TV</b>!
 - Support for more streaming apps - so that whatever you use, Flutter will be there to help you out with movie ratings!
 - We want Flutter to be your go-to pal for anything related to movies, documentaries and TV shows from ratings to recommendations.

<b>Concerns?</b>

Flutter uses Open Movie Database to get movie ratings so it is possible that it might not have the ratings of some not so popular movies.
Open Movie Database has ratings gathered from various popular websites like Imdb.
Flutter is not endorsed by or affiliated with Imdb.com or omdbapi.com
For any concerns, queries, or feedback, write to us at flutter.rating@gmail.com

-----
