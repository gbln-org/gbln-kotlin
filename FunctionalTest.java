/*
 * Functional test for Kotlin JNI bindings against libgbln.dylib
 * Compile: javac FunctionalTest.java
 * Run: java -Djava.library.path=../../core/ffi/libs/macos-arm64 FunctionalTest
 */

public class FunctionalTest {

    static {
        System.loadLibrary("gbln");
    }

    // Native method declarations
    private static native int gbln_parse(String input, long[] outValue);
    private static native String gbln_to_string(long valuePtr);
    private static native void gbln_value_free(long valuePtr);
    private static native String gbln_get_error_message();

    private static native boolean gbln_value_is_i8(long valuePtr);
    private static native boolean gbln_value_is_string(long valuePtr);
    private static native boolean gbln_value_is_object(long valuePtr);
    private static native boolean gbln_value_is_array(long valuePtr);

    private static native byte gbln_value_as_i8(long valuePtr);
    private static native String gbln_value_as_string(long valuePtr);

    private static native long gbln_object_get(long objectPtr, String key);
    private static native int gbln_object_len(long objectPtr);

    private static native int gbln_array_len(long arrayPtr);
    private static native long gbln_array_get(long arrayPtr, int index);

    private static int passed = 0;
    private static int failed = 0;

    private static void test(String name, TestCase testCase) {
        System.out.print("Test: " + name + " ... ");
        try {
            if (testCase.run()) {
                System.out.println("✅ PASS");
                passed++;
            } else {
                System.out.println("❌ FAIL");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("❌ EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
    }

    interface TestCase {
        boolean run();
    }

    public static void main(String[] args) {
        test("Parse simple integer", () -> {
            long[] outValue = new long[1];
            int result = gbln_parse("age<i8>(25)", outValue);
            if (result != 0) {
                System.out.println("\nParse failed: " + gbln_get_error_message());
                return false;
            }
            long ptr = outValue[0];
            boolean isI8 = gbln_value_is_i8(ptr);
            byte value = gbln_value_as_i8(ptr);
            gbln_value_free(ptr);
            return isI8 && value == 25;
        });

        test("Parse simple string", () -> {
            long[] outValue = new long[1];
            int result = gbln_parse("name<s32>(Alice)", outValue);
            if (result != 0) return false;
            long ptr = outValue[0];
            boolean isString = gbln_value_is_string(ptr);
            String value = gbln_value_as_string(ptr);
            gbln_value_free(ptr);
            return isString && "Alice".equals(value);
        });

        test("Parse object", () -> {
            long[] outValue = new long[1];
            String input = "user{id<u32>(12345) name<s64>(Alice)}";
            int result = gbln_parse(input, outValue);
            if (result != 0) return false;
            long ptr = outValue[0];
            boolean isObject = gbln_value_is_object(ptr);
            int len = gbln_object_len(ptr);

            long namePtr = gbln_object_get(ptr, "name");
            String name = gbln_value_as_string(namePtr);

            gbln_value_free(ptr);
            return isObject && len == 2 && "Alice".equals(name);
        });

        test("Parse array", () -> {
            long[] outValue = new long[1];
            String input = "tags<s16>[kotlin jvm android]";
            int result = gbln_parse(input, outValue);
            if (result != 0) return false;
            long ptr = outValue[0];
            boolean isArray = gbln_value_is_array(ptr);
            int len = gbln_array_len(ptr);

            long firstPtr = gbln_array_get(ptr, 0);
            String first = gbln_value_as_string(firstPtr);

            gbln_value_free(ptr);
            return isArray && len == 3 && "kotlin".equals(first);
        });

        test("Parse UTF-8 string", () -> {
            long[] outValue = new long[1];
            String input = "city<s16>(北京)";
            int result = gbln_parse(input, outValue);
            if (result != 0) return false;
            long ptr = outValue[0];
            String value = gbln_value_as_string(ptr);
            gbln_value_free(ptr);
            return "北京".equals(value);
        });

        test("Parse nested object", () -> {
            long[] outValue = new long[1];
            String input = "response{status<u16>(200) data{user{name<s32>(Alice)}}}";
            int result = gbln_parse(input, outValue);
            if (result != 0) return false;
            long ptr = outValue[0];

            long dataPtr = gbln_object_get(ptr, "data");
            long userPtr = gbln_object_get(dataPtr, "user");
            long namePtr = gbln_object_get(userPtr, "name");
            String name = gbln_value_as_string(namePtr);

            gbln_value_free(ptr);
            return "Alice".equals(name);
        });

        test("Serialise to string", () -> {
            long[] outValue = new long[1];
            String input = "name<s32>(Bob)";
            int result = gbln_parse(input, outValue);
            if (result != 0) return false;
            long ptr = outValue[0];

            String serialised = gbln_to_string(ptr);
            gbln_value_free(ptr);

            return serialised != null && serialised.contains("Bob");
        });

        test("Error handling - integer out of range", () -> {
            long[] outValue = new long[1];
            String input = "age<i8>(999)";
            int result = gbln_parse(input, outValue);
            return result != 0;  // Should fail
        });

        System.out.println("\n" + "=".repeat(50));
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        System.out.println("=".repeat(50));

        if (failed > 0) {
            System.exit(1);
        }
    }
}
