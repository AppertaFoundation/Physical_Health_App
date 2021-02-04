package com.staircase13.apperta.api.errors;

public class ErrorConstants {
    public static final String HTTP_400_API_DESCRIPTION = "Invalid Request. Specific issues are shown in Sub Errors";
    public static final String HTTP_400_MESSAGE = "Invalid Request. Specific issues are shown in Sub Errors";
    public static final String HTTP_401_API_DESCRIPTION = "Unauthorized. OAuth token required, or an invalid or expired token has been provided";
    public static final String HTTP_403_API_DESCRIPTION = "Forbidden. User associated with the given OAuth token does not have permission to access this resource";
    public static final String HTTP_404_API_DESCRIPTION = "Not found";
    public static final String HTTP_405_API_DESCRIPTION = "Method Not Allowed";
    public static final String HTTP_500_API_DESCRIPTION = "An Unexpected Server Error has occurred";
    public static final String HTTP_500_MESSAGE = "An Unexpected Server Error has occurred";
}
