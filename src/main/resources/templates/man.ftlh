<#import "parts/common.ftlh" as c>
<#import "parts/login.ftlh" as l>

<@c.page>
<div>
    <@l.logout />
    <span><a href="/user">User list</a> </span>
</div>

<div>
<form method="post">
    <input type="text" name="text" placeholder="Enter message" />
    <input type="text" name="tag" placeholder="Tag" />
    <input type="hidden" name="_csrf" value="${_csrf.token}" />
    <button type="submit">Add message</button>

</form>
</div>

<div>List of messages</div>
<form method="get" action="/man">
        <input type="text" name = "filter" value="${filter}">
        <#--<input type="hidden" name="_csrf" value="{{_csrf.token}}" />-->
        <button type="submit">Find</button>
</form>
<#list messages as message>
<div>
    <b>${message.id}</b>
    <span>${message.text}</span>
    <i>${message.tag}</i>
        <strong>${message.authorName}</strong>
</div>
    <#else>
    No message
</#list>
</@c.page>