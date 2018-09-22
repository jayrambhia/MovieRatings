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

`RATINGS_ENDPOINT` is Flutter's private server to obtain accurate ratings. You may keep it as empty string and it will fallback to OMDB api. If you wish to contribute to the project, please send me an email and I can give you a temporary api key.

### What can you contribute?

 - Bug fixes and improvements
 - Features (make sure that you are assigned to it)
 - Design changes (If you're a designer and would like to improve the designs, please get in touch)
 - Architecture changes (Please discuss what you want to improve before working on it)
 - Support for more apps (You're the best!)
 - Any other changes? Please get in touch.

### Beginners?

Are you new to android development? I encourage you to contribute to the project. If you find some issue that you want to fix but don't know where to start? I'd by happy to guide you.

### Guidelines

 - Kotlin only (no Java)
 - Try to follow the standard programming guidelines