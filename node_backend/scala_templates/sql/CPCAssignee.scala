val assigneeDF = spark.read
    .format("csv")
    .option("header", "true")
    .option("multiline", "true")
    .option("escape", "\"")
    .load("/patent/uspto/csv/g_assignee_disambiguated.csv")
    .createOrReplaceTempView("assignee_disambiguated")

val cpcDF = spark.read
    .format("csv")
    .option("header", "true")
    .option("multiline", "true")
    .option("escape", "\"")
    .load("/patent/uspto/csv/g_cpc_current.csv")
    .filter(col("cpc_group").contains("G06F9/30036"))
    .createOrReplaceTempView("cpc_current")

val df = spark.sql(
    """
    |SELECT *
    |FROM assignee_disambiguated JOIN cpc_current ON assignee_disambiguated.patent_id = cpc_current.patent_id
    |""".stripMargin)



// Create a temporary view for the filtered DataFrame
df.createOrReplaceTempView("temp_table")

// Count the number of rows for each value in the "cpc_group" column
val rowCounts = spark.sql(
    """
    |SELECT disambig_assignee_organization, COUNT(*) AS count
    |FROM temp_table
    |GROUP BY disambig_assignee_organization
    """.stripMargin)

// Sort the counts in descending order
val sortedCounts = rowCounts.orderBy(desc("count"))

sortedCounts.show(70)

// Convert sorted counts to JSON and collect as a list
val sortedString = sortedCounts.limit(100).toJSON.collectAsList().toString()

sortedString