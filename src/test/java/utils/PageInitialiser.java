package utils;

import pages.LoginPage;

/**
 * Page objects are held per thread, so parallel scenarios never share a page
 * (or the driver behind it). Use loginPage() instead of a static loginPage field.
 */
public class PageInitialiser {

    private static final ThreadLocal<LoginPage> LOGIN_PAGE = new ThreadLocal<>();

    public static void initializePageObjects() {
        LOGIN_PAGE.set(new LoginPage());
    }

    public static LoginPage loginPage() {
        LoginPage page = LOGIN_PAGE.get();
        if (page == null) {
            throw new IllegalStateException("Page objects not initialised on thread "
                    + Thread.currentThread().getName() + " - did the @Before hook open the browser?");
        }
        return page;
    }

    public static void clearPageObjects() {
        LOGIN_PAGE.remove();
    }
}
