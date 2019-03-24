package com.wisethan.ble;


public class Utils {


    public static void XMLparser(){
        try {
            final int STEP_NONE = 0 ;
            final int STEP_NO = 1 ;
            final int STEP_NAME = 2 ;

            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser() ;
            FileInputStream fis = new FileInputStream("file.xml") ;
            int step = STEP_NONE ;
            int no = -1 ;
            String name = null ;

            parser.setInput(fis, null) ;

            int eventType = parser.getEventType() ;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    // XML 데이터 시작
                } else if (eventType == XmlPullParser.START_TAG) {
                    String startTag = parser.getName() ;
                    if (startTag.equals("NO")) {
                        step = STEP_NO ;
                    } else if (startTag.equals("NAME")) {
                        step = STEP_NAME ;
                    } else {
                        step = STEP_NONE ;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String endTag = parser.getName() ;
                    if ((endTag.equals("NO") && step != STEP_NO) ||
                            endTag.equals("NAME") && step != STEP_NAME))
                    {
                        // TODO : error.
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText() ;
                    if (step == STEP_NO) {
                        no = Integer.parseInt(text) ;
                    } else if (step == STEP_NAME) {
                        name = text ;
                    }
                }

                eventType = parser.next();
            }

            if (no == -1 || name == null) {
                // ERROR : XML is invalid.
            }
        } catch (Exceptin e) {
            e.printStackTrace() ;
        }
    }







    //Property로 저장
    public static void WriteToProperty() {
        File file = new File(Environment.getDataDirectory() + "/data/" + getPackageName(), mDeviceName);

        FileOutputStream fos = null;
        try {
            //property 파일이 없으면 생성
            if (!file.exists()) {
                file.createNewFile();
            }

            fos = new FileOutputStream(file);

            //Property 데이터 저장
            Properties props = new Properties();
            props.setProperty("test", "Property에서 데이터를 저장");   //(key , value) 로 저장
            props.store(fos, "Property Test");

            Log.d("prop", "write success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Property로 불러오기
    public static String ReadToProperty() {
        //property 파일
        File file = new File(Environment.getDataDirectory() + "/data/" + getPackageName(), mDeviceName);

        if (!file.exists()) {
            return "";
        }

        FileInputStream fis = null;
        String data = "";
        try {
            fis = new FileInputStream(file);

            //Property 데이터 읽기
            Properties props = new Properties();
            props.load(fis);
            data = props.getProperty("test1", "");  //(key , default value)

            Log.d("prop", "read success");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }


}