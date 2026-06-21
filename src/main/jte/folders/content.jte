@import com.dewarim.cinnamon.model.Folder
@import com.dewarim.cinnamon.model.ObjectSystemData
@import java.util.List
@param Folder folder
@param String folderPath
@param List<Folder> subFolders
@param List<ObjectSystemData> osds
@param boolean latestHead
<div class="p-3">
    <div class="level mb-3">
        <div class="level-left">
            <div class="level-item">
                <nav class="breadcrumb" aria-label="breadcrumbs">
                    <ul>
                        <li><a href="/ui/folders">Home</a></li>
                        <li class="is-active"><a href="#" aria-current="page">${folder.getName()}</a></li>
                    </ul>
                </nav>
            </div>
        </div>
        <div class="level-right">
            <div class="level-item">
                <a class="button is-small is-success mr-2" href="/ui/folder/create?parentId=${folder.getId()}">+ Folder</a>
                <a class="button is-small is-primary mr-2" href="/ui/osd/create?parentId=${folder.getId()}">+ Document</a>
            </div>
            <div class="level-item">
                <div class="select is-small">
                    <label for="filter">
                        <select name="filter"
                                onchange="location.href='/ui/folders?folderPath=${folderPath}&filter=' + this.value">
                            @if(latestHead)
                                <option value="latestHead" selected>Latest version</option>
                                <option value="all">All versions</option>
                            @else
                                <option value="latestHead">Latest version</option>
                                <option value="all" selected>All versions</option>
                            @endif
                        </select>
                    </label>
                </div>
            </div>
        </div>
    </div>

    @if(!subFolders.isEmpty())
        <p class="heading">Subfolders</p>
        <div class="mb-3">
            <table class="table is-fullwidth is-hoverable is-narrow">
                <tbody>
                @for(Folder sub : subFolders)
                    <tr>
                        <td>
                            <a href="/ui/folders?folderPath=${folderPath}/${sub.getName()}"
                               hx-get="/ui/folders/navigate?folderPath=${folderPath}/${sub.getName()}"
                               hx-target="#folder-tree-panel"
                               hx-swap="innerHTML"
                               hx-push-url="/ui/folders?folderPath=${folderPath}/${sub.getName()}">📁 ${sub.getName()}</a>
                        </td>
                        <td class="is-narrow">
                            <a class="button is-small is-light" href="/ui/folder/edit?id=${sub.getId()}">Edit</a>
                        </td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    @endif

    @if(!osds.isEmpty())
        <p class="heading">Documents</p>
        <table class="table is-fullwidth is-hoverable is-narrow">
            <thead>
            <tr>
                <th>Name</th>
                <th>Version</th>
                <th>Size</th>
                <th>Modified</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            @for(ObjectSystemData osd : osds)
                <tr>
                    <td>${osd.getName()}</td>
                    <td><span class="tag is-light">${osd.getCmnVersion()}</span></td>
                    <td class="is-size-7 has-text-grey">
                        @if(osd.getContentSize() != null && osd.getContentSize() > 0)
                            ${osd.getContentSize()} B
                        @else
                            —
                        @endif
                    </td>
                    <td class="is-size-7 has-text-grey">${osd.getModified().toString()}</td>
                    <td>
                        <button class="button is-small is-info is-light"
                                hx-get="/ui/osd/meta?osdId=${osd.getId()}"
                                hx-target="#osd-meta-panel"
                                hx-swap="innerHTML">
                            Meta
                        </button>
                        <a class="button is-small is-light" href="/ui/osd/view?id=${osd.getId()}">View</a>
                    </td>
                </tr>
            @endfor
            </tbody>
        </table>
    @endif

    @if(subFolders.isEmpty() && osds.isEmpty())
        <p class="has-text-grey is-size-7">This folder is empty.</p>
    @endif
</div>
