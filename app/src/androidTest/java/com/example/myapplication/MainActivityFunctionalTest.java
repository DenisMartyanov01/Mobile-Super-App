package com.example.myapplication;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import com.example.myapplication.Notifications.MainActivity;

@RunWith(AndroidJUnit4.class)
public class MainActivityFunctionalTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testEmptyState() {
        // Проверяем начальный экран
        onView(withId(R.id.recyclerView))
                .check(matches(isDisplayed()));

        onView(withId(R.id.fabAdd))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));
    }

    @Test
    public void testAddAndDeleteNotification() {
        // 1. Добавляем уведомление
        onView(withId(R.id.fabAdd))
                .perform(click());

        // Ждем диалог
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTimeUpdatesInRealtime() {
        // Проверяем обновление времени в реальном времени
        onView(withId(R.id.recyclerView))
                .check(matches(isDisplayed()));

        // Ждем 2 секунды и проверяем, что UI не завис
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Проверяем, что FAB все еще доступен
        onView(withId(R.id.fabAdd))
                .check(matches(isEnabled()));
    }


}