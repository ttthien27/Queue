package com.android.queue.firebase.realtimedatabase;


import android.provider.BaseColumns;

/**
 This class is a contract for all possible entries in firebase realtime database
 **/
public class QueueDatabaseContract {

    static public class UserEntry {
        /**
         * Name of the root of database entry for User
         */
        static public final String ROOT_NAME = "users";

        /**
         * Phone of the user
         * <p>
         * Type: String
         */
        public final static String PHONE_ARM = "phone";

        /**
         * User's full name.
         * <p>
         * Type: String
         */
        public final static String FULL_NAME_ARM = "fullName";

        /**
         * User's password.
         * <p>
         * Type: String
         */
        public final static String PASSWORD_ARM = "password";

        /**
         * User's account create date.
         * <p>
         * Type: LONG (TIMESTAMP)
         */
        public final static String CREATE_DATE_ARM = "createDate";

        /**
         * Check if user is login.
         * <p>
         * Type: Boolen
         */
        public final static String IS_LOGIN_ARM = "isLogin";


        /**
         * Check if user create a room.
         * <p>
         * Type: Boolen
         */
        public final static String IS_HOST_ARM = "isHost";

        /**
         * Current room id that user is joined or created
         * <p>
         * Type: String
         */
        public final static String CURRENT_ROOM_ARM = "currentRoomId";

    }

    static public class RoomEntry {
        /**
         * Name of the root of database entry for Rooms
         */
        static public final String ROOT_NAME = "rooms";

        /**
         * Room's data. The entry store data of the room.
         * <p>
         * Type: RoomData (Model)
         */
        public final static String ROOM_DATA_ARM = "roomData";

        static public class RoomDataEntry {
            /**
             * Name of the root of RoomData Entry
             */
            static public final String ROOT_NAME = "roomData";

            /**
             * Room's Name
             * <p>
             * Type: String
             */
            public final static String ROOM_NAME_ARM = "roomName";

            /**
             * Room's create date
             * <p>
             * Type: LONG (TIMESTAMP)
             */
            public final static String CREATE_DATE_ARM = "createDate";

            /**
             * Room's address
             * <p>
             * Type: String
             */
            public final static String ROOM_ADDRESS_ARM = "address";

            /**
             * Room's time start, is the time where the room starts to do their jobs.
             * <p>
             * Type: LONG (TIMESTAMP)
             */
            public final static String TIME_START_ARM = "timeStart";

            /**
             * Room's maximum waiters.
             * <p>
             * Type: LONG
             */
            public final static String MAX_PARTICIPANT_ARM = "maxParticipant";

            /**
             * Room's QR Code file image name in firebase storage.
             * <p>
             * Type: String
             */
            public final static String QR_ARM = "qr";

            /**
             * Room's time wait. The time that the room processing for each waiter
             * <p>
             * Type: DOUBLE
             */
            public final static String TIME_WAIT_ARM = "timeWait";

            /**
             * Room's time delay. The time that the room processing delay for each waiter
             * <p>
             * Type: DOUBLE
             */
            public final static String TIME_DELAY_ARM = "timeDelay";

            /**
             * Room's wait setting
             * <p>
             * Type: String
             */
            public final static String WAIT_SETTING_ARM = "waitSetting";

            /**
             * Possible value for wait setting constant. The time wait by each users will be fit statically by the host
             * <p>
             * Type: String
             */
            public final static String CONSTANT_WAIT = "CONSTANT";
            /**
             * Possible value for wait setting balance. The time wait for each users will be the average time for all users had been waited
             * <p>
             * Type: String
             */
            public final static String BALANCE_WAIT = "BALANCE";

            /**
             * Room's latitude
             * <p>
             * Type: DOUBLE
             */
            public final static String LATITUDE_ARM = "Latitude";

            /**
             * Room's Longitude
             * <p>
             * Type: DOUBLE
             */
            public final static String LONGITUDE_ARM = "longitude";

            /**
             * Room's current wait number. The number in the room that is currently processed
             * <p>
             * Type: LONG
             */
            public final static String CURRENT_WAIT_ARM = "currentWait";

            /**
             * Room's host's phone.
             * <p>
             * Type: String
             */
            public final static String HOST_PHONE_ARM = "hostPhone";

            /**
             * Room's total waiter has been left.
             * <p>
             * Type: LONG
             */
            public final static String TOTAL_LEFT_ARM = "totalLeft";

            /**
             * Room's total waiter has been skip.
             * <p>
             * Type: LONG
             */
            public final static String TOTAL_SKIP_ARM = "totalSkip";

            /**
             * Room's total waiter has been processed.
             * <p>
             * Type: LONG
             */
            public final static String TOTAL_DONE_ARM = "totalDone";

            /**
             * Room's total participants.
             * <p>
             * Type: LONG
             */
            public final static String TOTAL_PARTICIPANT_ARM = "totalParticipant";


            /**
             * Room's state. Determine whether the room is close or not. If it is closed by the host. The waiter can't join this room any more.
             * <p>
             * Type: Boolen
             */
            public final static String IS_CLOSE_ARM = "isClose";

            /**
             * Room's state. Determine whether the room is pause or not. If it is pauses by the host. The waiter can't join this room in a certain amount of time.
             * <p>
             * Type: Boolen
             */
            public final static String IS_PAUSE_ARM = "isPause";

        }


        /**
         * Room's participant list. The list that records all the waiter in the room.
         * <p>
         * Type: Dictionary
         */
        public final static String PARTICIPANT_LIST_ARM = "participantList";

        /**
         * Room's participant list. The list that records all the waiter in the room.
         * <p>
         * Type: Dictionary
         */

        static public class ParticipantListEntry {

            public final static String ROOT_NAME = "participantList";
            /**
             * Waiter's phone.
             * <p>
             * Type: String
             */
            public final static String WAITER_PHONE_ARM = "waiterPhone";
            /**
             * Waiter's name.
             * <p>
             * Type: String
             */
            public final static String WAITER_NAME_ARM = "waiterName";

            /**
             * Waiter's ordinal number in the queue .
             * <p>
             * Type: String
             */
            public final static String WAITER_NUMBER_ARM = "waiterNumber";

            /**
             * Waiter's state in the queue .
             * <p>
             * Type: String
             */
            public final static String WAITER_STATE_ARM = "waiterState";

            /**
             * Possible value for waiter's state. "IsDone" mean that the users has been preprocessed
             * <p>
             * Type: String
             */
            public final static String STATE_IS_DONE = "IsDone";

            /**
             * Possible value for waiter's state. "IsLeft" mean that the users has been left the queue
             * <p>
             * Type: String
             */
            public final static String STATE_IS_LEFT = "IsLeft";

            /**
             * Possible value for waiter's state. "IsWait" mean that the users still need to wait for their turn.
             * <p>
             * Type: String
             */
            public final static String STATE_IS_WAIT = "IsWait";

            /**
             * Possible value for waiter's state. "IsSkip" mean that the users has been skil their turn by the host.
             * <p>
             * Type: String
             */
            public final static String STATE_IS_SKIP = "IsSkip";

        }
    }
}
