package or.nevet.cloudMess;

import android.provider.BaseColumns;

public class MesSqLite {
    private MesSqLite(){

    }

    public static final class MesSqLitEntry implements BaseColumns {

        public static final String TABLE_NAME = "Messes";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_MESS = "Mess";
        public static final String COLUMN_TIME = "Time";
        public static final String COLUMN_REPLY = "Reply";

    }

}
