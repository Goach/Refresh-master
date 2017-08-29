
# Refresh-master
仿今日头条下拉刷新效果

#1.0版本暂时支持RecyclerView，ScrollView控件的下拉刷新，后续支持CoordinationLayout控件的兼容

依赖方法

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```
	dependencies {
	        compile 'com.github.Goach:Refresh-master:1.0.0'
	}
```

#简单实用
- ScrollView

<com.goach.refreshlayout.widget.PullRefreshLayout
        android:id="@+id/ttRefreshLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
            <ScrollView
                android:id="@+id/scrollView"
                android:layout_width="match_parent"
                android:layout_height="match_parent">
		<LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_margin="10dp"
                    android:orientation="vertical"
                    android:background="#FAFAFA">
		    .....
		</LinearLayout>
            </ScrollView>
    </com.goach.refreshlayout.widget.PullRefreshLayout>
    
    
    
    
- RecyclerView

<com.goach.refreshlayout.widget.PullRefreshLayout
    android:id="@+id/ttRefreshLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <android.support.v7.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layoutManager="LinearLayoutManager"/>
</com.goach.refreshlayout.widget.PullRefreshLayout>

- CoordinationLayout

<?xml version="1.0" encoding="utf-8"?>
<android.support.design.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >
    <android.support.design.widget.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="256dp">
        <android.support.design.widget.CoordinatorLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layout_scrollFlags="scroll|exitUntilCollapsed">
            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:scaleType="centerCrop"
                android:background="@android:color/darker_gray"
                app:layout_collapseMode="parallax"
                />
            <android.support.v7.widget.Toolbar
                android:id="@+id/toolbar"
                android:layout_width="match_parent"
                android:layout_height="?attr/actionBarSize"
                app:popupTheme="@style/ThemeOverlay.AppCompat.Light"
                app:layout_collapseMode="pin" />
        </android.support.design.widget.CoordinatorLayout>
    </android.support.design.widget.AppBarLayout>
    <com.goach.refreshlayout.widget.PullRefreshLayout
        android:id="@+id/ttRefreshLayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">
            <RelativeLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent">
                <android.support.v7.widget.RecyclerView
                    android:id="@+id/recyclerView"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    app:layoutManager="LinearLayoutManager"/>
            </RelativeLayout>
    </com.goach.refreshlayout.widget.PullRefreshLayout>
</android.support.design.widget.CoordinatorLayout>
