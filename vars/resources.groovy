def getInputStream(resourcePath) {
  def input = libraryResource(resourcePath)
  return new ByteArrayInputStream(input.getBytes())
}

def getProperties(resourcePath) {
  def stream = getInputStream(resourcePath)
  def properties = new Properties()
  properties.load(stream)
  return properties
}


/**
  * Generates a path to a temporary file location, ending with {@code path} parameter.
  *
  * @param path path suffix
  * @return path to file inside a temp directory
  */
@NonCPS
String createTempLocation(String path) {
  String tmpDir = pwd tmp: true
  return tmpDir + File.separator + new File(path).getName()
}

/**
  * Returns the path to a temp location of a script from the global library (resources/ subdirectory)
  *
  * @param source path within the resources/ subdirectory of this repo
  * @param destination destination path (optional)
  * @param overwrite file (optional)
  * @return path to local file
  */
String copyResourceFile(Map args) {
  def destination = args.destination ?: createTempLocation(args.source)
  def testDestination = new File(destination)
  try {
    if(!testDestination.exists() || args.overwrite == true) {
      writeFile file: destination, text: libraryResource(args.source)
      echo "copyResourceFile: copied ${args.source} to ${destination}"
    } else {
      echo "copyResourceFile: ${destination} already exists... to replace use copyResourceFile(overwrite: true)"
    }
  } catch(err) {
    echo("Unable to locate file: ${args.source}")
    echo err.toString()
  }
  return destination
}
