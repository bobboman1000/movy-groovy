package core.structs;

public class Result {
    public final boolean added;
    public final SpotifyAlbum album;

    public Result(boolean added, SpotifyAlbum album) {
        this.added = added;
        this.album = album;
    }
}