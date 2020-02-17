package com.kitesoft.ex35xmlpullparsermovie;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //영화진흥위원회 openapi(http://www.kobis.or.kr/kobisopenapi/homepg/main/main.do) : 주간영화순위, 일간영화순위
    // [   ID: mrhi     pw: asdf1234      email : 96kite@naver.com   ]

    //일간 영화순위 정보를 제공받기 위해 영화진흥위원회 openAPI로부터 발급받은 apiKey [ 각자 본인 ID로 직접 발급받아 실습진행 ]
    String apiKey="4e573df404c65d869e804193f868eafb";

    ListView listView;
    ArrayAdapter adapter;
    ArrayList<String> items= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView= findViewById(R.id.listview);
        adapter= new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }

    public void clickBtn(View v){

        //네트워크를 통해서 xml문서를 읽어오기..[ Internet PERMISSION 주의!! ]
        //네트워크 작업은 MainThread가 수행하지 못하도록 되어 있으므로 별도의 Thread객체 생성 및 실행
        new Thread(){
            @Override
            public void run() {

                Date date= new Date();
                date.setTime( date.getTime()-(24*60*60*1000) );// 1일전 ( 일일박스오피스는 1일전까지만 확인할 수 있음)
                SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");

                String dateStr=sdf.format(date);
                //String dateStr="20181215";

                //xml읽기 작업 수행
                String address="http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.xml"
                        +"?key="+apiKey
                        +"&targetDt="+dateStr
                        +"&itemPerPage=10";

                //###############################################################################################################################
                //api28버전 부터 http경로에 대한 보안강화로 <application의 속성으로  usesCleartextTraffic="true" 속성을 추가해야함. ////////////////////
                //###############################################################################################################################


                try {
                    //네트워크를 통해 데이터를 주고받으려면 이 문서에서 서버까지 데이터를 보내주는 통로가 필요함.
                    //마치 영화토르의 지구와 아스가르드를 연결하는 무지개로드 같은 통로가 필요함. 이를 STREAM(스트림)이라고 함 - 음악서비스에서 '스트리밍서비스' 라는 용어를 통해 익숙한 표현
                    //[참고로 Stream이라는 통로는 단방향 흐름만 가능하기에... 서버로부터 내 앱으로 정보를 가져올때는 InpuStream이 필요하고, 반대로 앱에서 서버로 데이터를 보낼때는 OutpuStream이 필요함

                    //Stream을 열어주는 해임달(토르 캐릭터) 같은 객체(URL) 생성
                    URL url= new URL(address); //생성자 파라미터로 stream을 연결할 주소 전달

                    //생성자로 전달받은 주소(addree)의 서버와 무지개로드(Stream) 열기
                    InputStream is= url.openStream(); //서버로부터 데이터를 받아오는 것이므로 InputStream.
                    InputStreamReader isr= new InputStreamReader(is); //위 InputStream이 byte단위로 데이터를 읽어오기에 앱에서 화면에 글씨를 보여주기 번거로워서 문자단위를 읽어주는 InputStreamReader로 변환


                    //XML을 분석(parse)해주는 객체 생성
                    XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
                    XmlPullParser xpp= factory.newPullParser();
                    xpp.setInput(isr); //xml분석가에게 서버와 연결된 InputStreamReader를 설정해주면 이를 통해 서버에서 보내준 xml 데이터문서를 읽어옴.

                    //xpp를 이용해서 xml문서 분석 시작!
                    int eventType= xpp.getEventType();

                    StringBuffer buffer=null;

                    //xml문서의 끝까지 반복적으로 읽어와서 분석하기
                    while (eventType != XmlPullParser.END_DOCUMENT ){

                        switch ( eventType ){
                            case XmlPullParser.START_DOCUMENT:

                                //별도의 Thread는 화면에 보이는 작업(UI작업)을 수행할 수 없기에 Toast도 보이는 것이 불가능하여 runOnUiThread()를 이용
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "파싱시작!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                break;

                            case XmlPullParser.START_TAG:
                                String name= xpp.getName();//태그이름

                                if( name.equals("dailyBoxOffice") ){
                                    buffer= new StringBuffer();
                                }else if( name.equals("rank")){
                                    buffer.append("순위:");
                                    xpp.next();
                                    buffer.append( xpp.getText()+"\n");

                                }else if( name.equals("movieNm")){
                                    buffer.append("제목:");
                                    xpp.next();
                                    buffer.append( xpp.getText()+"\n");

                                }else if( name.equals("openDt")){
                                    buffer.append("개봉일:");
                                    xpp.next();
                                    buffer.append( xpp.getText()+"\n");

                                }else if( name.equals("audiAcc")){
                                    buffer.append("누적관객수:");
                                    xpp.next();
                                    buffer.append( xpp.getText()+"\n");
                                }

                                break;

                            case XmlPullParser.TEXT:
                                break;

                            case XmlPullParser.END_TAG:
                                String tag= xpp.getName();
                                if( tag.equals("dailyBoxOffice")){

                                    //일일 박스오피스의 영화1개당 List Item하나가 되도록 ListView에서 보여줄 대량의 데이터( ArrayList - items)에 읽어온 영화1개의 정보 추가
                                    items.add( buffer.toString() );

                                    //새로 리스트뷰에 보여줄 항목이 1개 추가되었으므로 리스트뷰 갱신[adapter.notifyDataSetChanged()]
                                    //리스트뷰의 갱신도 UI작업이므로 Thread가 직접 못하고 runOnUiThread()를 통해 갱신
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();//리스트뷰 갱신
                                        }
                                    });
                                }
                                break;
                        }

                        eventType= xpp.next();
                    }//while

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }

            }//run method...
        }.start();

    }//clickBtn method..

}//MainActivity class...
