package edu.temple.abrowser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class BrowserActivity extends AppCompatActivity implements PageControlFragment.PageControlInterface, PageViewerFragment.PageViewerInterface, BrowserControlFragment.BrowserControlInterface, PagerFragment.PagerInterface, PageListFragment.PageListInterface {

    FragmentManager fm;

    private final String PAGES_KEY = "pages";

    PageControlFragment pageControlFragment;
    BrowserControlFragment browserControlFragment;
    PageListFragment pageListFragment;
    PagerFragment pagerFragment;
    ImageButton bookmarks;
    ImageButton save;
    ArrayList<PageViewerFragment> pages;
    Bundle extras;
    String urlStr;

    Intent i;
    boolean listMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState != null){
            pages = (ArrayList) savedInstanceState.getSerializable(PAGES_KEY);

        }
        else
            pages = new ArrayList<>();

        fm = getSupportFragmentManager();

        listMode = findViewById(R.id.page_list) != null;

        Fragment tmpFragment;

        // If PageControlFragment already added (activity restarted) then hold reference
        // otherwise add new fragment. Only one instance of fragment is ever present
        if ((tmpFragment = fm.findFragmentById(R.id.page_control)) instanceof PageControlFragment)
            pageControlFragment = (PageControlFragment) tmpFragment;
        else {
            pageControlFragment = new PageControlFragment();
            fm.beginTransaction()
                    .add(R.id.page_control, pageControlFragment)
                    .commit();
        }

        // If BrowserFragment already added (activity restarted) then hold reference
        // otherwise add new fragment. Only one instance of fragment is ever present
        if ((tmpFragment = fm.findFragmentById(R.id.browser_control)) instanceof BrowserControlFragment)
            browserControlFragment = (BrowserControlFragment) tmpFragment;
        else {
            browserControlFragment = new BrowserControlFragment();
            fm.beginTransaction()
                    .add(R.id.browser_control, browserControlFragment)
                    .commit();
        }

        // If PagerFragment already added (activity restarted) then hold reference
        // otherwise add new fragment. Only one instance of fragment is ever present
        if ((tmpFragment = fm.findFragmentById(R.id.page_viewer)) instanceof PagerFragment)
            pagerFragment = (PagerFragment) tmpFragment;
        else {
            pagerFragment = PagerFragment.newInstance(pages);
            fm.beginTransaction()
                    .add(R.id.page_viewer, pagerFragment)
                    .commit();
        }


        // If fragment already added (activity restarted) then hold reference
        // otherwise add new fragment IF container available. Only one instance
        // of fragment is ever present
        if (listMode) {
            if ((tmpFragment = fm.findFragmentById(R.id.page_list)) instanceof PageListFragment)
                pageListFragment = (PageListFragment) tmpFragment;
            else {
                pageListFragment = PageListFragment.newInstance(pages);
                fm.beginTransaction()
                        .add(R.id.page_list, pageListFragment)
                        .commit();
            }
        }

        save = findViewById(R.id.btnsave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMsg();
            }
        });
        bookmarks = findViewById(R.id.btnBM);
        bookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToBookmarks();
            }
        });

     //   pages.add(PageViewerFragment.newInstance("https://google.com"));
        extras = getIntent().getExtras();
        if (extras != null){
            pages.add(PageViewerFragment.newInstance("https://google.com"));
        }
        pages.add(PageViewerFragment.newInstance("https://temple.edu"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        pages.clear();
    }

    public void goToBookmarks(){
        Intent i = new Intent(this, BookmarkActivity.class);
        startActivity(i);
    }
    public void saveMsg(){
        Toast.makeText(BrowserActivity.this, "Bookmark Saved", Toast.LENGTH_SHORT).show();
    }
    /**
     * Clear the url bar and activity title
     */
    private void clearIdentifiers() {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");
        pageControlFragment.updateUrl("");
    }

    // Notify all observers of collections
    private void notifyWebsitesChanged() {
        pagerFragment.notifyWebsitesChanged();
        if (listMode)
            pageListFragment.notifyWebsitesChanged();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        pages = null;
        // Save list of open pages for activity restart
        outState.putSerializable(PAGES_KEY, pages);
    }

    /**
     * Update WebPage whenever PageControlFragment sends new Url
     * Create new page first if none exists
     * Alternatively, you can create an empty page when the activity first loads
     * @param url to load
     */
    @Override
    public void go(String url) {
        if (pages.size() > 0)
            pagerFragment.go(url);
        else {
            pages.add(PageViewerFragment.newInstance(url));
            notifyWebsitesChanged();
            pagerFragment.showPage(pages.size() - 1);
        }

    }

    /**
     * Go back to previous page when user presses Back in PageControlFragment
     */
    @Override
    public void back() {
        pagerFragment.back();
    }

    /**
     * Go forward to next page when user presses Forward in PageControlFragment
     */
    @Override
    public void forward() {
        pagerFragment.forward();
    }

    /**
     * Update displayed Url in PageControlFragment when Webpage Url changes
     * only if it is the currently displayed page, and not another page
     * @param url to display
     */
    @Override
    public void updateUrl(String url) {
        if (url != null && url.equals(pagerFragment.getCurrentUrl())) {
            pageControlFragment.updateUrl(url);

            // Update the ListView in the PageListFragment - results in updated titles
            notifyWebsitesChanged();
        }
    }

    /**
     * Update displayed page title in activity when Webpage Url changes
     * only if it is the currently displayed page, and not another page
     * @param title to display
     */
    @Override
    public void updateTitle(String title) {
        if (title != null && title.equals(pagerFragment.getCurrentTitle()) && getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);

        // Results in the ListView in PageListFragment being updated
        notifyWebsitesChanged();
    }

    /**
     * Add a new page/fragment to the list and display it
     */
    @Override
    public void newPage() {
        // Add page to list
        pages.add(new PageViewerFragment());
        // Update all necessary views
        notifyWebsitesChanged();
        // Display the newly created page
        pagerFragment.showPage(pages.size() - 1);
        // Clear the displayed URL in PageControlFragment and title in the activity
        clearIdentifiers();
    }

    /**
     * Display requested page in the PagerFragment
     * @param position of page to display
     */
    @Override
    public void pageSelected(int position) {
        pagerFragment.showPage(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            String textMessage = "https://temple.edu";
            Intent sendIntent = new Intent();
          //  sendIntent.setAction(Intent.ACTION_SEND);

          //  sendIntent.setData(Uri.parse(url));
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, textMessage);
            sendIntent.setType("text/plain");
            String title = "Select the browser";
            Intent chooser = Intent.createChooser(sendIntent, title);
            if(sendIntent.resolveActivity(getPackageManager()) != null){
                startActivity(chooser);
            }
        }
        return super.onOptionsItemSelected(item);
    }

}