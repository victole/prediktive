# Victor Toledo code example for prediktive

the project contains code developed by Victor Toledo for various Adobe AEM projects
I have created the basic project structure using Maven AEM Project Archetype version 35
The project and code is not ready to work on AEM

Brief explanation of the requirement
-----------
One of the features of the project was to have articles with news for end users and notify them when a new article is published.

Once the author published the article, the end user should receive a push notification in his/her user profile.

Backend code explanation
-----------
I developed an event handler (ActivationEventHandler.java) that when publishing an article-type resource triggered a workflow (/var/workflow/models/new-article) and this workflow had the necessary logic to know what type of user should be notified (DetermineNotifiedUserTypeWorkflowProcess.java), because the application had different types of users and then delegate (WorkflowDelegationProcess.java) the process notification to a new workflow step process with the user type as parameter (NotificationWorkflowProcess.java). The notification was saved to the AWS Dynamo DB instance.

In the first step the users group will be determined based on the Article page properties and according to that configuration the “Notify External Users of New Article” and/or “Notify Internal Users of New Article” will be executed.

The “Determine User Type To Be Notified” step uses the DetermineNotifiedUserTypeWorkflowProcess class which is in charge to “translate” the page properties definition to the workflow models.

And the “Workflow Delegation” will be in charge to execute the desired workflow models. (WorkflowDelegationProcess.java)

The main code is in
core/src/main/java/com/victole/examples/core/services
core/src/main/java/com/victole/examples/core/models

AEM Component explanation
-----------
ui.apps/src/main/content/jcr_root/apps/example/components/promo-articles

This component has a vue.js component to handle the behavior

Author can configure in dialog:

- the component title
- the tags to filter the pages
- only pages with one of the selected tags will be shown
- the base search path for articles
- the layout being two options:

View More Articles

Load More
the number of articles listed: 3, 6, 9 or 12.
