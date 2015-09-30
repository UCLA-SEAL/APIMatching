# API_Matching

Mapping code elements in one version of a program to corresponding code elements in another version is a fundamental building block for many software engineering tools. Existing tools that match code elements or identify structural changes--refactorings and API changes--between two versions of a program have two limitations that we overcome. First, existing tools cannot easily disambiguate among many potential matches or refactoring candidates. Second, it is difficult to use these tools' results for various software engineering tasks due to an unstructured representation of results. To overcome these limitations, our approach represents structural changes as a set of high-level change rules, automatically infers likely change rules and determines method-level matches based on the rules. By applying our tool to several open source projects, we show that our tool identifies matches that are difficult to find using other approaches and produces more concise results than other approaches. Our representation can serve as a better basis for other software engineering tools.

Reference:

[Automatic Inference of Structural Changes for Matching across Program Versions, Miryung Kim, David Notkin, Dan Grossman, ICSE '07](http://web.cs.ucla.edu/~miryung/Publications/icse07-apirule.pdf)
