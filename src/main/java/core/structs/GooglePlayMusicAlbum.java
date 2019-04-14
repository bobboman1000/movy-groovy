package core.structs;

public class GooglePlayMusicAlbum {
    public final String artist;
    public final String title;

    public GooglePlayMusicAlbum(String artist, String title) {
        this.artist = artist;
        this.title = title;
    }

    @Override public boolean equals(Object other) {
        if (other instanceof GooglePlayMusicAlbum) {
            return this.title.equals(((GooglePlayMusicAlbum) other).title) && this.artist.equals(((GooglePlayMusicAlbum) other).artist);
        } else return false;
    }
}
