package core.userinterface;

import com.github.felixgail.gplaymusic.api.GPlayMusic;
import com.github.felixgail.gplaymusic.model.Track;
import com.github.felixgail.gplaymusic.util.TokenProvider;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyApiThreading;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.requests.data.library.SaveAlbumsForCurrentUserRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import core.structs.GooglePlayMusicAlbum;
import core.structs.Result;
import core.structs.SpotifyAlbum;
import core.userinterface.terminal.Terminal;
import core.util.Option;
import core.util.Try;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;

public class CommandLineInterface {
    private Terminal terminal;

    public CommandLineInterface(Terminal terminal) {
        this.terminal = terminal;
    }

    public void start() {
        terminal.put("Yeah we running");
        terminal.put("Please enter your GPM Credentials");

        String email = forceGetNextString("Email:");
        String password = forceGetNextString("Password:");
        String imei = forceGetNextString("imei of an android device with gpm installed:");

        Try<GPlayMusic> possibleGpmApi = getGPMApi(email, password, imei);
        SpotifyApi spotifyApi= getSpotifyApi("a", "b", URI.create("test"));

        possibleGpmApi.onSuccess(gpmApi -> {
            terminal.put("Fetching albums...");
            Stream<GooglePlayMusicAlbum> googlePlayMusicAlbums = getAlbumsFrom(gpmApi);

            long found = googlePlayMusicAlbums.count();
            terminal.put("Distinct albums found: " + found); // this is materialized anyway so who cares

            // partially apply the function to always use the same client
            Function<GooglePlayMusicAlbum, Future<Try<SpotifyAlbum>>> toSpotifyAlbum =
                    (album) -> fetchSpotifyAlbumId(album, spotifyApi);

            // map gpm albums to spotify albums
            Stream<Future<Try<SpotifyAlbum>>> spotifyAlbums =
                    googlePlayMusicAlbums.map(toSpotifyAlbum);

            Function<Future<Try<SpotifyAlbum>>, Future<Result>> addToLibrary =
            spotifyAlbums.map(addToLibrary);
        });

        // albums.map(album -> addToSpotify(album));
    }

    private String forceGetNextString(String prompt) {
        return terminal.getString(prompt)
                .orElseGet(() -> {
                    terminal.put("Input was invalid, try again");
                    return forceGetNextString(prompt);
                });
    }

    private Try<GPlayMusic> getGPMApi(String email, String password, String imei) {
        return Try.applyThrowing(() -> TokenProvider.provideToken(email, password, imei))
                .map(token ->
                        new GPlayMusic.Builder()
                                .setAuthToken(token)
                                .build()
                );
    }

    private SpotifyApi getSpotifyApi(String clientId, String clientSecret, URI redirectUri) {
        return new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();
    }

    private Stream<GooglePlayMusicAlbum> getAlbumsFrom(GPlayMusic api) {
        Stream<Track> tracks =
                Try.applyThrowing(() -> api.getTrackApi()
                        .getLibraryTracks()
                        .stream()
                ).getOrElse(Stream.empty());

        return tracks
                .map(track -> new GooglePlayMusicAlbum(track.getAlbumArtist(), track.getAlbum()))
                .distinct();
    }

    private Future<Result> addToSpotifyAccount(SpotifyAlbum album, SpotifyApi api) {
        SaveAlbumsForCurrentUserRequest apiRequest = api
                .saveAlbumsForCurrentUser(album.id)
                .build();

        Callable<Option<String>> mapped = () -> Try.applyThrowing(apiRequest::execute)
                .map(Option::of)
                .toOption()
                .flatten();

        return SpotifyApiThreading.executeAsync(mapped);
    }

    private Future<Try<SpotifyAlbum>> fetchSpotifyAlbumId(GooglePlayMusicAlbum gpmAlbum, SpotifyApi api) {
        String query = gpmAlbum.artist + gpmAlbum.title; // this is subject to another ticket

        // there's executeAsync but no map so we need to register our own callback. Thanks java
        SearchAlbumsRequest request = api.searchAlbums(query).build();
        Callable<Try<SpotifyAlbum>> mapped = () ->
                Try.applyThrowing(request::execute).flatMap(results -> {
                    AlbumSimplified[] albums = results.getItems();
                    Try<AlbumSimplified> first = Try.apply(() -> albums[0]); // good old out of bounds

                    if (first.isSuccess())
                        return first.map(album ->
                                // lets assume that every album has at least 1 artist...
                                new SpotifyAlbum(album.getArtists()[0].getName(), album.getName(), album.getId())
                        );
                    else
                        return Try.failed(new Exception("Couldn't find album: " + query));
                });

        // java futures aren't allowed to fail and result in a failed future, therefore we need to wrap our
        // potentially failing opartion in a try
        return SpotifyApiThreading.executeAsync(mapped);
    }
}
