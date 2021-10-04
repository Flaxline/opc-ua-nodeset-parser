package com.digitalpetri.opcua.nodeset;

import com.digitalpetri.opcua.nodeset.attributes.NodeAttributes;
import com.google.common.collect.Multimap;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

public class UaNodeSetMergerTest {

    static UaNodeSet nodeSet1; // independent
    static UaNodeSet nodeSet2; // references 1
    static UaNodeSet nodeSet3; // independent

    @Before
    public void setup() throws Exception {
        nodeSet1 = UaNodeSet.parse(new FileInputStream(new File("src/test/resources/mergertest/nodeset1.xml")));
        nodeSet2 = UaNodeSet.parse(new FileInputStream(new File("src/test/resources/mergertest/nodeset2.xml"))); // references 1
        nodeSet3 = UaNodeSet.parse(new FileInputStream(new File("src/test/resources/mergertest/nodeset3.xml"))); // independent
    }

    /**
     * checks basic merge without any relations to be adapted
     * @throws Exception
     */
    @Test
    public void TestMergeNoRelations() throws Exception {
        UaNodeSet result = UaNodeSetMerger.merge(nodeSet1, nodeSet3);

        // check new namespace table
        UShort idx1 = result.getNamespaceTable().getIndex("http://unittests.org/nodeset1/");
        UShort idx2 = result.getNamespaceTable().getIndex("http://yourorganisation.org/nodeset3/");

        // check nodes

        Assert.assertNotNull(result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=5001")));
        Assert.assertEquals("TestNode", result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=5001")).getBrowseName().getName());
        Assert.assertEquals(idx1, result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=5001")).getBrowseName().getNamespaceIndex());

        Assert.assertNotNull(result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=5001")));
        Assert.assertEquals("TestNode3", result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=5001")).getBrowseName().getName());
        Assert.assertEquals(idx2, result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=5001")).getBrowseName().getNamespaceIndex());

    }

    /**
     * Checks that rewriting of relations works correctly
     * @throws Exception
     */
    @Test
    public void TestMergeWithRelations() throws  Exception {
        UaNodeSet result = UaNodeSetMerger.merge(nodeSet1, nodeSet2);

        // check new namespace table
        UShort idx1 = result.getNamespaceTable().getIndex("http://unittests.org/nodeset1/");
        UShort idx2 = result.getNamespaceTable().getIndex("http://yourorganisation.org/nodeset2/");

        // check nodes and references
        NodeAttributes ns1 = result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=7001"));
        Assert.assertNotNull(result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=7001")));
        Assert.assertEquals("TestMethod", result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=7001")).getBrowseName().getName());
        Assert.assertEquals(idx1, result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=7001")).getBrowseName().getNamespaceIndex());
        NodeAttributes ns2 = result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=7001"));
        Assert.assertNotNull(result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=7001")));
        Assert.assertEquals("TestMethod2", result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=7001")).getBrowseName().getName());
        Assert.assertEquals(idx2, result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=7001")).getBrowseName().getNamespaceIndex());

        Multimap<NodeId, Reference> refs = result.getExplicitReferences();
        Collection<Reference> r1 = refs.get(NodeId.parse("ns=" + idx1 + ";i=5001"));
        Assert.assertEquals(3, r1.size());
        Assert.assertTrue(r1.stream().anyMatch(r -> r.getTargetNodeId().equals(ExpandedNodeId.parse("ns=" + idx1 + ";i=6001"))));

        Collection<Reference> r2 = refs.get(NodeId.parse("ns=" + idx2 + ";i=5003"));
        Assert.assertEquals(5, r2.size());
        Assert.assertTrue(r2.stream().anyMatch(r -> r.getTargetNodeId().equals(ExpandedNodeId.parse("ns=" + idx1 + ";i=6001"))));
        Assert.assertTrue(r2.stream().anyMatch(r -> r.getTargetNodeId().equals(ExpandedNodeId.parse("ns=" + idx1 + ";i=7001"))));
        Assert.assertTrue(r2.stream().anyMatch(r -> r.getTargetNodeId().equals(ExpandedNodeId.parse("ns=" + idx2 + ";i=7001"))));

    }

    /**
     * Checks that rewriting of relations works in the other direction as well
     * @throws Exception
     */
    @Test
    public void TestMergeWithRelationsReverse() throws  Exception {
        UaNodeSet result = UaNodeSetMerger.merge(nodeSet2, nodeSet1);

        // check new namespace table
        UShort idx2 = result.getNamespaceTable().getIndex("http://unittests.org/nodeset1/");
        UShort idx1 = result.getNamespaceTable().getIndex("http://yourorganisation.org/nodeset2/");

        // check nodes and references
        NodeAttributes ns1 = result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=7001"));
        Assert.assertNotNull(result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=7001")));
        Assert.assertEquals("TestMethod2", result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=7001")).getBrowseName().getName());
        Assert.assertEquals(idx1, result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=7001")).getBrowseName().getNamespaceIndex());
        NodeAttributes ns2 = result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=7001"));
        Assert.assertNotNull(result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=7001")));
        Assert.assertEquals("TestMethod", result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=7001")).getBrowseName().getName());
        Assert.assertEquals(idx2, result.getNodes().get(NodeId.parse("ns=" + idx2 + ";i=7001")).getBrowseName().getNamespaceIndex());

        Multimap<NodeId, Reference> refs = result.getExplicitReferences();
        Collection<Reference> r1 = refs.get(NodeId.parse("ns=" + idx2 + ";i=5001"));
        Assert.assertEquals(3, r1.size());
        Assert.assertTrue(r1.stream().anyMatch(r -> r.getTargetNodeId().equals(ExpandedNodeId.parse("ns=2;i=6001"))));

        Collection<Reference> r2 = refs.get(NodeId.parse("ns=" + idx1 + ";i=5003"));
        Assert.assertEquals(5, r2.size());
        Assert.assertTrue(r2.stream().anyMatch(r -> r.getTargetNodeId().equals(ExpandedNodeId.parse("ns=" + idx2 + ";i=6001"))));
        Assert.assertTrue(r2.stream().anyMatch(r -> r.getTargetNodeId().equals(ExpandedNodeId.parse("ns=" + idx2 + ";i=7001"))));
        Assert.assertTrue(r2.stream().anyMatch(r -> r.getTargetNodeId().equals(ExpandedNodeId.parse("ns=" + idx1 + ";i=7001"))));

    }

    /**
     * checks how merge of a nodeset with itself behaves
     * @throws Exception
     */
    @Test
    public void TestMergeWithDuplicate() throws Exception {
        UaNodeSet result = UaNodeSetMerger.merge(nodeSet1, nodeSet1);

        // check new namespace table
        Assert.assertEquals(UShort.valueOf(1), result.getNamespaceTable().getIndex("http://unittests.org/nodeset1/"));

        // check nodes
        Assert.assertEquals(nodeSet1.getNodes().size(), result.getNodes().size());
        Assert.assertEquals(nodeSet1.getNamespaceTable(), result.getNamespaceTable());
        Assert.assertEquals(nodeSet1.getAliasTable(), result.getAliasTable());
        Assert.assertEquals(nodeSet1.getDataTypeDefinitions(), result.getDataTypeDefinitions());

        // references double
        Assert.assertEquals(nodeSet1.getCombinedReferences().size()*2, result.getCombinedReferences().size());
        Assert.assertEquals(nodeSet1.getExplicitReferences().size()*2, result.getExplicitReferences().size());
        Assert.assertEquals(nodeSet1.getImplicitReferences().size()*2, result.getImplicitReferences().size());

    }

    /**
     * Checks that basic repeated merge does not mess up node IDs
     * @throws Exception
     */
    @Test
    public void TestMergeWithThree() throws Exception {
        UaNodeSet result = UaNodeSetMerger.merge(nodeSet1, nodeSet2);
        result = UaNodeSetMerger.merge(result, nodeSet3);

        // check new namespace table
        UShort idx1 = result.getNamespaceTable().getIndex("http://unittests.org/nodeset1/");
        UShort idx2 = result.getNamespaceTable().getIndex("http://yourorganisation.org/nodeset2/");
        UShort idx3 = result.getNamespaceTable().getIndex("http://yourorganisation.org/nodeset3/");
        Assert.assertNotEquals(idx1, idx2);
        Assert.assertNotEquals(idx1, idx3);
        Assert.assertNotEquals(idx2, idx3);

        NodeAttributes ns1 = result.getNodes().get(NodeId.parse("ns=" + idx1 + ";i=5001"));
        Assert.assertEquals("TestNode", ns1.getBrowseName().getName());
        Assert.assertEquals(idx1, ns1.getBrowseName().getNamespaceIndex());
        NodeAttributes ns3 = result.getNodes().get(NodeId.parse("ns=" + idx3 + ";i=5001"));
        Assert.assertEquals("TestNode3", ns3.getBrowseName().getName());
        Assert.assertEquals(idx3, ns3.getBrowseName().getNamespaceIndex());
    }

}
