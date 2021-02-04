import { StyleSheet } from "react-native";
import colors from "./Colors";

export default StyleSheet.create({
  container: {
    height: "100%",
    width: "100%",
    backgroundColor: colors.mainBackground
  },
  header: {
    fontSize: 24,
    marginBottom: 20,
    color: colors.highlightColorTwo
  },
  headerWhite: {
    fontSize: 20,
    textAlign: "center",
    margin: 20,
    color: colors.highlightColorTwo
  },
  headerWhiteLeft: {
    fontSize: 24,
    color: colors.darkGrey
  },
  subheaderWhiteLeft: {
    fontSize: 15,

    color: colors.darkGrey,
    marginTop: 20
  },
  normal: {
    fontSize: 20,
    color: "white"
  },
  darkNormal:{
    fontSize: 20,
    color: colors.darkGrey
  },
  highlightNormal : {
    fontSize: 20,
    color: colors.highlightColorTwo
  },
  body: {
    color: "#333333",
    marginBottom: 20,
    fontSize: 16
  },
  button: {
    marginTop: 10
  },
  backgroundImage: {
    height: "100%",
    width: "100%"
  },
  subContainer: {
    padding: 20,
    marginBottom: 20,
    backgroundColor: colors.secondaryBackground
  },
  bottom: {
    flex: 2,
    justifyContent: "flex-end",
    paddingLeft:10,
    paddingRight:10
  },
  textcontainer: {
    borderWidth: 2, // size/width of the border
    borderLeftColor: "lightgrey", // color of the border.
    borderRightColor: "lightgrey", // color of the border.
    borderTopColor: "lightgrey", // color of the border.
    paddingLeft: 20,
    paddingRight: 20,
    paddingTop: 20,
    paddingBottom: 10,
    backgroundColor: "white"
  },
  textcontainernarrow: {
    borderWidth: 2, // size/width of the border
    borderLeftColor: "lightgrey", // color of the border.
    borderRightColor: "lightgrey", // color of the border.
    borderTopColor: "lightgrey", // color of the border.
    padding: 20,
    backgroundColor: "white",
    flex: 1
  },
  scrollcontainer: {
    flex: 1,
    flexDirection: "column",
    padding: 20
  },
  textFieldLabelText: {
     color: colors.darkGrey, marginLeft: 20, marginTop: 5
     },
  dashboardItemDivider:{
    height:4, backgroundColor:colors.highlightColorTwo
  }   
});
