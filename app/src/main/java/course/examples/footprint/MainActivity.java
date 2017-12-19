package course.examples.footprint;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    private ListFragment listFragment;
    private MapFragment mapFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listFragment = new ListFragment();
        mapFragment = new MapFragment();
        accountFragment = new AccountFragment();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, listFragment).commitAllowingStateLoss();//replace detail fragment

    }

    // bottom navigation on selected listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                // when home item selected, goto list fragment
                case R.id.navigation_list:
                {
                    //mTextMessage.setText(R.string.title_list);
                    Toast.makeText(getApplicationContext(),"List",Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, listFragment).commitAllowingStateLoss();//replace detail fragment
                    return true;
                }
                case R.id.navigation_map:
                {
                    //mTextMessage.setText(R.string.title_map);
                    Toast.makeText(getApplicationContext(),"map",Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, mapFragment).commitAllowingStateLoss();//replace detail fragment
                    return true;
                }
                case R.id.navigation_account:
                {
                    //mTextMessage.setText(R.string.title_account);
                    Toast.makeText(getApplicationContext(),"account",Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction().replace(R.id.mainFrameLayout, accountFragment).commitAllowingStateLoss();//replace detail fragment
                    return true;
                }
            }
            return false;
        }
    };
}
