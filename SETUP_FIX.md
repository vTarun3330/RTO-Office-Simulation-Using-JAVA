# IDE Configuration Fix

Your Eclipse project settings are out of sync with the `pom.xml`. The project requires Java 21, but Eclipse is configured for Java 17.

## Steps to Fix

1.  **Right-click** on the project `OOAD Mini Project` in the **Package Explorer**.
2.  Select **Maven** > **Update Project...** (or press `Alt+F5`).
3.  Ensure your project is selected in the dialog.
4.  Check **Force Update of Snapshots/Releases**.
5.  Click **OK**.

This will force Eclipse to read the `pom.xml` again and configure the Java version to 21.

## If errors persist:
1.  Check **Window** > **Preferences** > **Java** > **Installed JREs**.
2.  Ensure a JDK 21 is installed and selected.
