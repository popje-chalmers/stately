package app;
import machine.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

public class IssuesListing extends JPanel implements StatelyListener
{
    private StatelyApp app;
    private JPanel issuesPanel;
    private JList<Issue> issueList;
    private Filter<Issue> filter;
    private ListCellRenderer<Issue> renderer;
    
    public IssuesListing(StatelyApp app)
    {
        this.app = app;
        app.addStatelyListener(this);

        setBackground(app.colors.editor_background);
        
        renderer = new IssueCellRenderer(app);
        issueList = new JList<>();
        issueList.setOpaque(false);
        issueList.setCellRenderer(renderer);
        
        issuesPanel = new JPanel();
        issuesPanel.setLayout(new BorderLayout());
        issuesPanel.setBackground(app.colors.issue_list_background);
        issuesPanel.setOpaque(true);
        
        JScrollPane scroller = new JScrollPane(
            issuesPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        setLayout(new BorderLayout());
        //add(Helper.makeTitle(app, "Issues", app.colors.title, null), BorderLayout.NORTH);
        add(scroller, BorderLayout.CENTER);
        
        rebuild();
    }

    public void rebuild()
    {
        

        java.util.List<Issue> allIssues = app.getMachine().getIssues();
        ArrayList<Issue> issues = new ArrayList<>();

        for(Issue i: allIssues)
        {
            if(filter == null || filter.matches(i))
            {
                issues.add(i);
            }
        }
        
        int count = issues.size();
        
        if(count == 0)
        {
            JLabel tmp = new JLabel("No issues!");
            tmp.setForeground(app.colors.issue_none);
            tmp.setOpaque(false);
            issuesPanel.removeAll();
            issuesPanel.setLayout(new FlowLayout());
            issuesPanel.add(tmp, BorderLayout.CENTER);
        }
        else
        {
            Issue[] items = issues.toArray(new Issue[issues.size()]);
            issueList.setListData(items);
            issuesPanel.removeAll();
            issuesPanel.setLayout(new BorderLayout());
            issuesPanel.add(issueList, BorderLayout.CENTER);
        }
        
        revalidate();
        repaint();
    }
    
    public void setFilter(Filter<Issue> f)
    {
        filter = f;
    }

    

    // StatelyListener

    public void machineModified(MachineEvent e)
    {
        rebuild();
    }

    public void machineSwapped(MachineEvent e)
    {
        rebuild();
    }

    public void selectionModified() {}
}
