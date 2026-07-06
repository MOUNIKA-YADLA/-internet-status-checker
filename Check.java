private static List<BitbucketFile> listBitbucketFiles(String projectKey, String repoName, String token, String path) throws IOException {
    List<BitbucketFile> files = new ArrayList<>();
    int start = 0;
    boolean isLastPage = false;

    while (!isLastPage) {
        String url = BITBUCKET_API_BASE + "/projects/" + projectKey + "/repos/" + repoName + "/files"
                + (path.isEmpty() ? "" : "/" + path)
                + "?limit=1000&start=" + start;

        String response = makeBitbucketRequest(url, token);

        if (response == null || response.isEmpty()) {
            break;
        }

        int valuesStart = response.indexOf("\"values\":");
        if (valuesStart >= 0) {
            String values = response.substring(valuesStart);
            parseBitbucketFilesDirectArray(values, files, path);
        }

        // Determine if this is the last page
        isLastPage = response.contains("\"isLastPage\":true");

        if (!isLastPage) {
            int nextStartIdx = response.indexOf("\"nextPageStart\":");
            if (nextStartIdx >= 0) {
                int numStart = nextStartIdx + "\"nextPageStart\":".length();
                int numEnd = numStart;
                while (numEnd < response.length() && Character.isDigit(response.charAt(numEnd))) {
                    numEnd++;
                }
                start = Integer.parseInt(response.substring(numStart, numEnd));
            } else {
                // no nextPageStart found, bail to avoid infinite loop
                break;
            }
        }
    }

    return files;
}
