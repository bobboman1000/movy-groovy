package core.userinterface;

import com.github.felixgail.gplaymusic.api.GPlayMusic;
import com.github.felixgail.gplaymusic.model.Album;
import com.github.felixgail.gplaymusic.model.Track;
import com.github.felixgail.gplaymusic.util.TokenProvider;
import core.userinterface.terminal.Terminal;
import core.util.Option;
import core.util.Try;
import svarzee.gps.gpsoauth.AuthToken;

import java.util.List;
import java.util.stream.Stream;

public class CommandLineInterface {
    private Terminal terminal;

    public CommandLineInterface(Terminal terminal) {
        this.terminal = terminal;
    }

    public void start() {
        while (true) {
            terminal.put("Yeah we running");
            terminal.put("Please enter your GPM Credentials");

            String email = forceGetNextString("Email:");
            String password = forceGetNextString("Password:");
            String imei = forceGetNextString("imei of an android device with gpm installed:");

            GPlayMusic api = getGPMApi(email, password, imei);

            Stream<String> albums = getAlbumsFrom(api);

            // albums.map(album -> addToSpotify(album));
        }
    }

    private String forceGetNextString(String prompt) {
        return terminal.getString(prompt)
                .orElseGet(() -> {
                    terminal.put("Input was invalid, try again");
                    return forceGetNextString(prompt);
                });
    }

    private GPlayMusic getGPMApi(String email, String password, String imei) {
        Try<GPlayMusic> possibleService =
                Try.applyThrowing(() -> TokenProvider.provideToken(email, password, imei))
                        .map(token ->
                                new GPlayMusic.Builder()
                                        .setAuthToken(token)
                                        .build()
                        )
                        .onFailure(exception -> {
                            terminal.put(exception.getMessage());
                            System.exit(0);
                        });

        return possibleService.get(); // this is safe because we exit on failure
    }

    private Stream<String> getAlbumsFrom(GPlayMusic api) {
        Stream<Track> tracks =
                Try.applyThrowing(() -> api.getTrackApi()
                        .getLibraryTracks()
                        .stream()
                ).getOrElse(Stream.empty());

        return tracks
                .map(track -> track.getAlbumArtist() + " " + track.getAlbum()) // this is subject to another ticket
                .distinct();
    }
}
