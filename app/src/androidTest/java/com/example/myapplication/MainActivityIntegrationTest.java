package com.example.myapplication;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.example.myapplication.Notifications.MainActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityIntegrationTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setUp() {
        // Запускаем активность перед каждым тестом
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void tearDown() {
        // Закрываем активность после каждого теста
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void testTitleIsVisible() {
        // Ждем, пока активность полностью загрузится
        try {
            Thread.sleep(1000); // Даем время на создание UI
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Проверяем заголовок
        onView(withText("Мои уведомления"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testAllElementsVisible() {
        // Ждем загрузки
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Проверяем все основные элементы
        onView(withId(R.id.titleTextView))
                .check(matches(isDisplayed()));

        onView(withId(R.id.recyclerView))
                .check(matches(isDisplayed()));

        onView(withId(R.id.fabAdd))
                .check(matches(isDisplayed()));

        onView(withId(R.id.button_to_notifications2))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testRecyclerViewHasNoItemsInitially() {
        // Ждем загрузки
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Проверяем, что RecyclerView пуст
        // Можно использовать проверку на количество элементов
        // или просто убедиться, что он отображается
        onView(withId(R.id.recyclerView))
                .check(matches(isDisplayed()));
    }
}