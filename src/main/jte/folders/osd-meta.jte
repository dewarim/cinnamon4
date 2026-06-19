@import com.dewarim.cinnamon.model.ObjectSystemData
@param ObjectSystemData osd
@param String ownerName
@param String modifierName
@param String aclName
@param String formatName
<div>
    <p class="heading mb-2">Document Metadata</p>
    <table class="table is-narrow is-fullwidth is-size-7">
        <tbody>
            <tr><th>Name</th><td>${osd.getName()}</td></tr>
            <tr><th>Version</th><td>${osd.getCmnVersion()}</td></tr>
            <tr><th>Format</th><td>${formatName}</td></tr>
            <tr><th>Size</th>
                <td>
                    @if(osd.getContentSize() != null && osd.getContentSize() > 0)
                        ${osd.getContentSize()} bytes
                    @else
                        No content
                    @endif
                </td>
            </tr>
            <tr><th>Created</th><td>${osd.getCreated().toString()}</td></tr>
            <tr><th>Modified</th><td>${osd.getModified().toString()}</td></tr>
            <tr><th>Owner</th><td>${ownerName}</td></tr>
            <tr><th>Modifier</th><td>${modifierName}</td></tr>
            <tr><th>ACL</th><td>${aclName}</td></tr>
        </tbody>
    </table>
    <p class="has-text-grey is-size-7">ID: ${osd.getId()}</p>
</div>
