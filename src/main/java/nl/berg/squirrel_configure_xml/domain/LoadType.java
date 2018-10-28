package nl.berg.squirrel_configure_xml.domain;

public enum LoadType {

    LOAD_BUT_DONT_CACHE(0), LOAD_AND_CACHE(1), DONT_LOAD(2);

    private final int loadTypeInt;

    private LoadType(int loadTypeInt) {
        this.loadTypeInt = loadTypeInt;
    }

    @Override
    public String toString() {
        return String.valueOf(loadTypeInt);
    }
}
