package scalax.io

import collection.Traversable
import util.matching.Regex

trait Path extends Location with DirectoryOpsMixin with FileOpsMixin { self =>
  /**
   * Convert this <code>Path</code> into a <code>Directory</code<
   * @return a <code>Directory</code> object representing this path
   * @throws IllegalStateException if this path is already a file
   */
  def asDirectory: Directory
  /**
   * Convert this <code>Path</code> into a <code>File</code>
   * @return a <code>File</code> object representing this path
   * @throws IllegalStateException if this path already exists as a directory
   */
  def asFile: File
  /**
   * @returns <code>None</code> if the path doesn't exist, or an appropriate typed object if it does
   */
  def asFileOrDirectory: Option[Either[File, Directory]]
  /**
   * Creates a new file if this <code>Path</code> does not exist
   * @return <code>Some(file)</code> if a file was created, otherwise <code>None</code>
   */
  def createNewFile(): Option[File]
  /**
   * Creates a new directory if this <code>Path</code> does not exist
   * @return <code>Some(directory)</code> if a directory was created, otherwise <code>None</code>
   */
  def createNewDirectory(): Option[Directory]

  // directory operations...maybe this should be in a mixin instead
  /** the full contents of this directory tree */
  def tree: Traversable[Path]
  /** */
  def contents: Traversable[Path]

  // file operations...maybe a mixin???
  /**
   * the length of the this <code>File</code> in bytes
   * @todo what should this return if the file does not exist?  java.io.File will return 0L,
   *       the same as for an empty file.
   */
  def length: Long
}
