class ApiException implements Exception {
  final String message;
  final int? statusCode;
  final dynamic data;
  final List<String>? validationErrors;

  ApiException({
    required this.message,
    this.statusCode,
    this.data,
    this.validationErrors,
  });

  @override
  String toString() => message;

  factory ApiException.fromDioError(dynamic error) {
    if (error is! Exception) {
      return ApiException(message: 'An unexpected error occurred: ${error.toString()}');
    }

    try {
      // Handle DioException dynamically to avoid tight coupling
      final dynamic err = error;
      final dynamic response = err.response;
      final int? statusCode = response?.statusCode;
      final dynamic responseData = response?.data;

      String message = 'Network communication failure';
      List<String>? validationErrors;

      if (responseData is Map<String, dynamic>) {
        if (responseData.containsKey('message')) {
          message = responseData['message']?.toString() ?? message;
        } else if (responseData.containsKey('error')) {
          message = responseData['error']?.toString() ?? message;
        }

        if (responseData['errors'] is List) {
          validationErrors = (responseData['errors'] as List)
              .map((e) => e.toString())
              .toList();
        } else if (responseData['errors'] is Map) {
          validationErrors = (responseData['errors'] as Map)
              .values
              .map((e) => e.toString())
              .toList();
        }
      } else if (responseData is String && responseData.isNotEmpty) {
        message = responseData;
      }

      switch (statusCode) {
        case 400:
          return ApiException(
            message: message.isNotEmpty ? message : 'Invalid request submission',
            statusCode: 400,
            validationErrors: validationErrors,
          );
        case 401:
          return ApiException(
            message: 'Session expired or unauthenticated. Please login again.',
            statusCode: 401,
          );
        case 403:
          return ApiException(
            message: 'Access denied. You do not have permission for this action.',
            statusCode: 403,
          );
        case 404:
          return ApiException(
            message: message.isNotEmpty ? message : 'Requested resource not found',
            statusCode: 404,
          );
        case 409:
          return ApiException(
            message: message.isNotEmpty ? message : 'Conflict in operation state',
            statusCode: 409,
          );
        case 500:
          return ApiException(
            message: 'Server error encountered. Please try again shortly.',
            statusCode: 500,
          );
        default:
          return ApiException(
            message: message,
            statusCode: statusCode,
            validationErrors: validationErrors,
          );
      }
    } catch (_) {
      return ApiException(message: 'Unable to connect to SamadhanX servers. Check your internet connection.');
    }
  }
}
