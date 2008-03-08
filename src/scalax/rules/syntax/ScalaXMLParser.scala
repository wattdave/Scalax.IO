// -----------------------------------------------------------------------------
//
//  Scalax - The Scala Community Library
//  Copyright (c) 2005-8 The Scalax Project. All rights reserved.
//
//  The primary distribution site is http://scalax.scalaforge.org/
//
//  This software is released under the terms of the Revised BSD License.
//  There is NO WARRANTY.  See the file LICENSE for the full text.
//
// -----------------------------------------------------------------------------

package scalax.rules.syntax

import Character._

/** An XML parser for Scala source code.
 *
 * @author Andrew Foggin
 *
 * based on Scala Language Specification.
 */
trait ScalaXMLParser extends ScalaScanner {
  
  def scalaPattern : Rule[Expression]
  def scalaExpr : Rule[Expression]
  
  val xmlNameStart = (elem('_')
      | unicode(LOWERCASE_LETTER) // Ll
      | unicode(UPPERCASE_LETTER) // Lu
      | unicode(OTHER_LETTER ) // Lo
      | unicode(TITLECASE_LETTER) //Lt
      | unicode(LETTER_NUMBER)) // Nl
      
  val xmlNameChar = (xmlNameStart | choice(":.-")
      | unicode(COMBINING_SPACING_MARK) // Mc
      | unicode(ENCLOSING_MARK) // Me
      | unicode(NON_SPACING_MARK) // Mn
      | unicode(MODIFIER_LETTER) // Lm
      | unicode(DECIMAL_DIGIT_NUMBER )) // Nd
      
  val xmlName = xmlNameStart ~++ (xmlNameChar*) ^^ toString
  val elementName = xmlName as "elementName"
  
  val xmlS = choice(" \t\r\n")+
  val xmlComment = "<!--" -~ anyChar *~- "-->" ^^ toString ^^ XMLComment as "xmlComment"
  val resolvedReference = "&amp;" -^ '&' | "&lt;" -^ '<' | "&gt;" -^ '>' | "&apos;" -^ '\'' | "&quot;" -^ '"'
  
  val startElement = '<' -~ (elementName&) as "startElement"
  val emptyElement = "/>" -^ None as "emptyElement"
  val tagEnd = '>' as "tagEnd"
  val endTag = "</" as "endTag"
  
  lazy val xmlExpr = endToken("xmlExpr", (xmlElement  | cDataSect | pi +) ^^ NodeList)
  lazy val xmlElement = startElement -~ elementName ~ (attribute*) ~- (xmlS?) >~> xmlElementRest
  def xmlElementRest(name : String, attributes : List[Attribute]) : Rule[XMLElement] = (emptyElement
      | tagEnd -~ (xmlContent  ^^ Some[Expression]) ~- endElement(name)) ^^ XMLElement(name, attributes)
  def endElement(name : String) = (endTag -~ elementName ~- (xmlS?) ~- tagEnd) filter (_ == name)
  lazy val xmlContent : Rule[Expression] = (xmlElement | xmlComment | charData | scalaExpr  | cDataSect | pi | entityRef *) ^^ NodeList

  lazy val xmlPattern = endToken("xmlPattern", startElement -~ elementName ~- (xmlS?) >> xmlPatternRest)
  def xmlPatternRest(name : String) : Rule[XMLPattern] = (emptyElement
      | tagEnd -~ xmlPatternContent ~- endElement(name)) ^^ XMLPattern(name)
  lazy val xmlPatternContent = (xmlPattern | xmlComment | charData | scalaPattern | cDataSect | pi | entityRef *) ^^ NodeList ^^ Some[Expression]

  lazy val cDataSect = "<![CDATA[" -~ anyChar *~- "]]>" ^^ toString ^^ CData
  lazy val pi = "<?" -~ xmlName ~ (xmlS -~ anyChar *~- "?>" ^^ toString | "?>" -^ "") ^~^ ProcessingInstruction
  lazy val entityRef = '&' -~ xmlName ~- ';' ^^ EntityRef

  val attributeName = xmlS -~ xmlName ~- '=' as "attributeName"
  val attributeValue : Rule[Expression] = (quoted('"') | quoted('\'') as "attributeValue") | scalaExpr
  def quoted(ch : Char) = ch -~ (resolvedReference | anyChar - choice("<&")) *~- ch ^^ toString ^^ StringLiteral
  
  val attribute = attributeName ~ attributeValue ^~^ Attribute
  val charData = ("{{" -^ '{' | resolvedReference | anyChar - ("]]>" | '{' | '<' | '&') +) ^^ toString ^^ TextNode
  

}